# ADR-0003: Refresh Token with One-Time-Use Rotation

**Status:** Accepted
**Date:** 2026-04-14

## Context

A spec `005-refresh-token-and-containers` introduziu fluxo de refresh token. Duas
estratégias canônicas estavam sobre a mesa:

1. **Refresh token estático**: o mesmo token vale até expirar; cliente reusa N vezes.
   Simples, mas se um refresh token vaza, atacante obtém acesso silencioso até a
   expiração natural do token.
2. **One-time use com rotation**: cada chamada a `/refresh` invalida o token apresentado
   e emite um par novo (access + refresh). Replay de um refresh já consumido é
   detectável e indica comprometimento.

Restrições do projeto:

- Article V da constituição exige refresh tokens com **rotation obrigatória**.
- O domínio precisa rastrear estado por token (consumido ou não), o que inviabiliza
  refresh tokens stateless tipo JWT auto-contido — exige persistência.
- O fluxo precisa cooperar com o status da conta (`enabled`, `accountLocked`) na hora
  do refresh, não apenas no login original.

## Decision

Implementar refresh token persistido com rotação one-time use no
`application/service/RefreshTokenService`:

- Entidade `RefreshToken` (`domain/model/RefreshToken.java`) com `token` (UUID string,
  unique), `userId`, `expiresAt`, `used` (boolean).
- A cada `/login` bem-sucedido: emite par `accessToken` (JWT) + `refreshToken` (UUID
  persistido).
- A cada `/refresh`:
  1. Localiza o token; ausente → `InvalidRefreshTokenException`.
  2. Se `used=true` → `InvalidRefreshTokenException` (replay detection, log com warn).
  3. Se expirado → `InvalidRefreshTokenException` (log com warn).
  4. Carrega `User` via `findByIdWithAuthorities` (evita N+1).
  5. Valida `enabled` e `accountLocked` no momento atual — não confia no estado do
     login original.
  6. Marca o refresh apresentado como `used=true` e persiste.
  7. Emite **novo** access + **novo** refresh; retorna `RefreshResult` (record).
- Tabela `refresh_token` indexada em `token` (unique) e `user_id`, criada via Flyway.
- Endpoint `/refresh` protegido por rate limit (Article V #5 da constituição).

## Consequences

### Positivas
- **Replay attack detectável**: tentativa de reuso de refresh consumido é log warn e
  retorna 401 — pode disparar alertas/revogação em massa em evolução futura.
- **Janela de comprometimento limitada**: vazamento de refresh só é útil até o cliente
  legítimo fazer o próximo refresh.
- Estado de conta (`enabled`, `accountLocked`) é re-verificado a cada rotação, fechando
  a janela em que um usuário desabilitado continuaria operando com tokens antigos.
- DTO de retorno é `record` (`RefreshResult`), aderente ao Article III.

### Negativas / Trade-offs aceitos
- **Storage obrigatório** para refresh tokens. A tabela cresce ao longo do tempo —
  necessita rotina de purge para tokens expirados/usados (item de roadmap operacional;
  não bloqueante na primeira versão).
- **Custo de I/O por refresh**: um SELECT + um UPDATE + um INSERT a cada rotação. Aceito;
  refresh é evento esparso comparado a chamadas autenticadas com access token.
- Cliente DEVE persistir o refresh atualizado a cada chamada — um cliente bugado que
  guarda o refresh anterior se autoinvalida. Documentado em `quickstart.md` da spec 005.

### Neutras / Implicações operacionais
- Detecção de replay (uso de token `used=true`) PODE evoluir para revogação em cascata
  de toda a família de tokens do usuário (ainda não implementado — registrar em ADR
  separado quando feito).
- Logs do service mascaram o token via `tokenId=` (UUID interno), nunca o `tokenValue`.
