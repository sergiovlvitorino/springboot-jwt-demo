# ADR-0005: Refresh Token Hashing, Account Lockout e XFF Restrito

**Status:** Accepted
**Date:** 2026-05-19

## Context

O code review da spec `005-refresh-token-and-containers` identificou um conjunto de
vulnerabilidades de segurança agrupadas em cinco achados (SEC-1 a SEC-5):

- **SEC-1**: Refresh tokens persistidos como UUID em texto claro — compromisso do banco
  expõe tokens válidos imediatamente.
- **SEC-2**: `LoginRateLimitFilter` lia `X-Forwarded-For` incondicionalmente — atacante
  podia variar o header XFF para bypassar rate limit (IP spoofing trivial).
- **SEC-3**: Ausência de mecanismo de lockout de conta — ataques de força bruta podiam
  testar credenciais indefinidamente sem bloqueio.
- **SEC-4**: Emails logados em texto claro — violação de PII em logs de aplicação e
  sistemas de observabilidade.
- **SEC-5**: Invalidação de tokens ao logout/lock não cobria tokens anteriores emitidos
  na mesma sessão — lacuna no racionamento de refresh token rotation.

Cada problema poderia ser endereçado isoladamente, mas compartilham a mesma camada
(`infrastructure/security`) e a mesma superfície de ataque (autenticação). A decisão
foi tratar em conjunto, documentando as estratégias como um ADR único.

## Decision

### SEC-1 — SHA-256 de refresh tokens sem salt

Adotaremos `RefreshTokenHasher` (utilitário estático) que aplica SHA-256 ao valor raw
do token antes de persistir. O cliente recebe o UUID raw; o banco armazena apenas o
hash (coluna `token_hash VARCHAR(64)`). Lookup via `findByTokenHash(hash)`.

Justificativa de não usar salt: tokens têm 128 bits de entropia (UUID v4), tornando
rainbow table attacks inviáveis sem salt. SHA-256 sem salt permite lookup direto por
hash sem custo de iteração (diferentemente de BCrypt para senhas, onde o custo é
intencional — tokens não são segredos derivados de senha humana).

### SEC-2 — XFF apenas com proxy confiável

`LoginRateLimitFilter` recebe `@Value("${security.trusted-proxy.enabled:false}")
boolean trustedProxyEnabled`. O header `X-Forwarded-For` só é consultado para extração
de IP quando `trustedProxyEnabled=true`. Default `false` — deployments sem reverse
proxy (dev local, containers sem LB) usam `getRemoteAddr()` diretamente.

### SEC-3 — Lockout de conta em memória + flag persistido

`LoginAttemptService` (`@Component`) mantém `ConcurrentHashMap<String, Integer>` com
contador por email (lowercase). Ao atingir `security.account-lockout.max-attempts`
(default 5), busca o usuário via `UserRepository.findByEmail`, seta
`accountLocked=true`, persiste e remove a entrada do mapa. O mapa tem guarda de
tamanho máximo (10.000 entradas) com clear defensivo para evitar OOM em ambiente sem
load balancer.

Decisão de não implementar auto-unlock: desbloqueio manual (via admin) é intencionalmente
mais seguro — auto-unlock por tempo cria janela previsível para retry de força bruta.

`JWTLoginFilter` integra `LoginAttemptService`:
- `AuthenticationException` → `loginFailed(username)` antes de propagar a exceção.
- `successfulAuthentication` → `loginSucceeded(auth.getName())` para resetar contador.

### SEC-4 — PII mascarada em logs

`PiiMasker.maskEmail(String)` (utilitário estático) retorna `p***@domain.com` — apenas
o primeiro caractere do local-part visível. Usado em todo logging de email no filtro
de autenticação.

### SEC-5 — Revogação em cadeia de tokens anteriores

A cada rotação de refresh token válida, `RefreshTokenService.revokeAllPreviousTokens(userId)`
marca como `used=true` todos os tokens não consumidos anteriores ao que está sendo
rotacionado. Isso garante que tokens emitidos em sessões paralelas sejam invalidados
quando qualquer uma delas faz refresh.

## Consequences

### Positivas

- **Compromisso de banco não expõe tokens válidos**: SHA-256 é irreversível; atacante
  com dump da tabela não consegue usar os hashes diretamente.
- **Rate limit robusto**: sem XFF não confiável, bypassar rate limit exige controle do
  IP de origem real (não apenas manipular um header HTTP).
- **Força bruta limitada**: lockout após N tentativas com flag persistido resiste a
  restart da aplicação.
- **Conformidade de PII**: logs não expõem endereços de e-mail completos.
- **Invalidação agressiva**: revogação em cadeia reduz a janela de abuso de tokens
  em sessões paralelas comprometidas.

### Negativas / Trade-offs aceitos

- **Lockout em memória é local**: em cluster horizontal, contadores não são compartilhados
  entre instâncias — atacante pode distribuir tentativas entre N pods. Aceito para
  primeira versão; evolução natural é Redis com TTL.
- **Auto-unlock ausente**: usuário legítimo bloqueado por erro de senha repetido precisa
  de intervenção de admin. Aceito — melhor usabilidade levemente prejudicada do que
  janela previsível de retry.
- **SHA-256 sem salt é adequado para tokens de alta entropia**: se o gerador de UUID
  for comprometido (entropia baixa), tokens poderiam ser quebráveis por força bruta.
  Aceito — UUID v4 com CSPRNG é pré-requisito implícito.
- **Revogação em cadeia tem custo de UPDATE**: O(n) tokens por usuário marcados como
  used a cada rotação. Aceito — rotação é evento esparso; tabela deve ter índice em
  `(user_id, used)` para minimizar scan.

### Neutras / Implicações operacionais

- `security.trusted-proxy.enabled=true` DEVE ser configurado explicitamente em deployments
  com reverse proxy/load balancer (nginx, AWS ALB, etc.) para que rate limit por IP
  real funcione corretamente.
- Migration V4 de Flyway adiciona coluna `token_hash` e remove `token`; irreversível
  após merge.
- Monitorar crescimento da tabela `refresh_token` — purge periódico de registros
  `used=true` ou expirados é operacionalmente necessário (roadmap: job agendado).
- Logs de lockout (`log.warn("Account locked: userId={}")`) devem ser monitorados como
  alerta de segurança em produção.
