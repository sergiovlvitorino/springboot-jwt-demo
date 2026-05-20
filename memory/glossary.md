# Glossário do Projeto

> Vocabulário oficial do Spring Boot JWT Demo. Toda spec, ADR, plan, task e
> mensagem de commit deve usar os termos abaixo conforme definidos.
> A consistência terminológica reduz ambiguidade em revisões e onboarding.

**Versão:** 1.0.0 — 2026-05-06
**Mantenedor:** Tech Lead do projeto

## Regras de manutenção

- Termos novos só entram via PR dedicado (`docs(glossary): add <termo>`) ou
  acoplados ao PR da feature que introduz o conceito.
- Quem pode adicionar: qualquer contribuidor; aprovação obrigatória de
  Tech Lead.
- Quando atualizar:
  - Toda vez que uma spec introduzir novo conceito de domínio, segurança,
    persistência ou operação que apareça em mais de um arquivo do repo.
  - Quando um ADR for aceito e introduzir um termo (ex.: `UserDetailsAdapter`
    veio do ADR-0002).
- Definições devem ter de 1 a 3 linhas. Detalhes longos vão na spec ou ADR
  referenciada no campo "Onde aparece".
- Ordene alfabeticamente por nome do termo.
- Se um termo for deprecado, marque como `**Deprecado em:** vX.Y.Z` em vez
  de remover (preserva histórico de revisões).

---

## Termos

### AC (Acceptance Criterion)

Critério verificável associado a uma spec, em formato `[AC-N]`, que define
passa/falha objetivo para a feature. Onde aparece: `specs/*/spec.md`,
`memory/constitution.md` (Article VI).

### Access Token

JWT de curta duração (1 hora em prod) emitido no login. Transportado pelo
cliente no header `Authorization: Bearer <token>`. Possui claim
`token_type: "access"`. Onde aparece: `specs/005-refresh-token-and-containers/spec.md`,
`memory/constitution.md` (Article V).

### ADR (Architecture Decision Record)

Registro versionado de decisão arquitetural significativa, em
`memory/decisions/NNNN-titulo.md`. Onde aparece:
`memory/constitution.md` (Article XII), `memory/decisions/`.

### Authority Granular

Permissão fina mapeada para uma operação específica (ex.: `USER_DELETE`,
`USER_SAVE`). Não há promoção implícita: ter `USER_SAVE` não autoriza
delete. Onde aparece: `specs/002-security-hardening/spec.md`,
`memory/constitution.md` (Article V).

### BusinessException

Hierarquia de exceções de negócio modelada como `sealed class` em
`domain/exception/`. Subclasses são `final` e cada uma tem handler HTTP
dedicado em `RestExceptionHandler`. Onde aparece:
`memory/decisions/0001-sealed-business-exception-hierarchy.md`,
`memory/constitution.md` (Article IV).

### Constitution

Documento `memory/constitution.md` que define princípios invioláveis do
projeto, versionado em semver. Mudanças exigem PR `chore(constitution): ...`,
justificativa e bump de versão. Onde aparece: `memory/constitution.md`.

### Definition of Done

Conjunto mínimo de critérios cumpridos para uma feature ser considerada
concluída: tríade SDD criada, ACs verdes, testes verdes, cobertura
mantida, ADRs registrados se aplicável. Onde aparece:
`memory/constitution.md` (Articles VI, XII).

### Fail-fast Env Var

Variáveis sensíveis declaradas como `${VAR:?}` no Docker Compose. O
container falha imediatamente se a variável não estiver definida —
elimina deploy silencioso com defaults inseguros. Onde aparece:
`docker-compose.yml`, `memory/constitution.md` (Article XI).

### Multi-stage Dockerfile

Imagem construída em dois estágios: `build` (com `eclipse-temurin:21-jdk` e
Maven) gera o JAR; `runtime` (com `eclipse-temurin:21-jre`) apenas executa,
rodando como non-root user. Onde aparece: `Dockerfile`,
`memory/constitution.md` (Article XI).

### PII Masking

Mascaramento de dados pessoais (e-mails, identificadores) antes de logar.
Implementado via `maskEmail()` em utilitário de logging. Onde aparece:
`memory/constitution.md` (Article V), `specs/002-security-hardening/spec.md`.

### Profile

Perfil Spring que seleciona configurações por ambiente: `dev` (H2 local),
`test` (H2 em memória, ativado em `src/test/resources/application.properties`)
e `prod` (PostgreSQL). Onde aparece: `memory/constitution.md` (Article IX),
`application*.properties`.

### Refresh Token

JWT de longa duração (default 7 dias) emitido junto do access token no
login. Retornado no body JSON da resposta de login (não em header).
One-time-use: consumido em `POST /auth/refresh` para obter novo par.
Onde aparece: `specs/005-refresh-token-and-containers/spec.md` (AC-1 a AC-10),
`memory/constitution.md` (Article V).

### RestExceptionHandler

Componente em `ui/rest/controller/` que mapeia cada subclasse de
`BusinessException` para um status HTTP e `errorCode` específicos. Sem
branch default genérico para erros de negócio. Onde aparece:
`memory/decisions/0001-sealed-business-exception-hierarchy.md`.

### Sealed Exception Hierarchy

Aplicação concreta de `sealed class` à `BusinessException`: hierarquia
fechada em tempo de compilação, força awareness do handler HTTP ao
adicionar nova exceção. Onde aparece:
`memory/decisions/0001-sealed-business-exception-hierarchy.md`,
`memory/constitution.md` (Article IV).

### Soft Delete

Marcação de registro como inativo (`enabled=false`) em vez de remoção
física. Padrão obrigatório para entidades de negócio. Hard delete é
proibido. Onde aparece: `memory/constitution.md` (Article V item 9).

### Spec / Plan / Tasks

Tríade obrigatória de artefatos por feature em `specs/NNN-feature-name/`:
`spec.md` (o quê e por quê), `plan.md` (como técnico) e `tasks.md`
(decomposição executável). Onde aparece: `specs/README.md`,
`memory/constitution.md` (Article XII).

### Spec Kit / Spec-Driven Development (SDD)

Processo de desenvolvimento adotado pelo projeto: descreva antes de codar,
em ordem `/specify → /clarify → /plan → /tasks → /analyze → /implement`.
Onde aparece: `specs/README.md`, `.specify/`.

### Token Rotation

Política obrigatória ao consumir um refresh token: o token usado é
invalidado (`used=true`) no mesmo passo em que o novo par é emitido.
Reuso do refresh anterior retorna 401. Onde aparece:
`specs/005-refresh-token-and-containers/spec.md` (AC-3),
`memory/constitution.md` (Article V item 4).

### UserDetailsAdapter

Ponte em `infrastructure/security/` entre `User` (entidade de domínio
puro) e `UserDetails` (contrato do Spring Security). Permite que o
domínio não dependa de Spring Security. Expõe o `User` original via
`getUser()`. Onde aparece:
`memory/decisions/0002-userdetails-adapter-domain-decoupling.md`,
`specs/005-refresh-token-and-containers/spec.md` (AC-11 a AC-16).

### Vendor-specific Migration

Migrations Flyway separadas por banco em `db/migration/h2/` e
`db/migration/postgresql/`. Cada vendor tem suas particularidades de
tipo (ex.: UUID em H2 vs BYTEA em PostgreSQL) e cláusulas DDL. Onde
aparece: `memory/constitution.md` (Articles I, VII),
`specs/003-database-migrations/spec.md`.
