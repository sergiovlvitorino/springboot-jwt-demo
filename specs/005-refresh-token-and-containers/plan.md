# Implementation Plan: Refresh Token & Containerization

**Spec:** [spec.md](./spec.md)
**Status:** Implemented

## Architecture

T9 (UserDetailsAdapter) é **fundacional** — bloqueia T6 (Refresh Token) porque
o filtro de login precisa do adapter para extrair o `User` do principal. T13
(Docker) é independente.

```
Phase 1 (paralelo):
  ├── @sre        → Dockerfile + compose (T13)
  ├── @dev-java   → UserDetailsAdapter (T9)
  └── @qa         → Plano de testes refresh token

  CRITICAL SYNC ──────────────────────────────────────────

Phase 2 (após T9 mergeado e testado):
  └── @dev-java + @qa → Refresh Token completo (T6)
```

## Components

### T9 — UserDetailsAdapter
| Componente | Mudança |
|------------|---------|
| `domain/model/User.java` | Remover `implements UserDetails` e métodos da interface |
| `infrastructure/security/UserDetailsAdapter.java` | NEW — wrapper |
| `application/service/UserService.java` | `loadUserByUsername` retorna adapter |
| `infrastructure/security/TokenAuthenticationService.java` | Usar `user.getEmail()` em vez de `getUsername()` |

### T13 — Docker
| Componente | Mudança |
|------------|---------|
| `Dockerfile` | NEW — multi-stage |
| `docker-compose.yml` | NEW — prod (app + db) |
| `docker-compose.dev.yml` | NEW — dev (apenas db) |
| `.dockerignore` | NEW |

### T6 — Refresh Token
| Componente | Mudança |
|------------|---------|
| `domain/model/RefreshToken.java` | NEW — entidade |
| `domain/repository/RefreshTokenRepository.java` | NEW — `findByToken` |
| `domain/exception/InvalidRefreshTokenException.java` | NEW — `final extends BusinessException` |
| `domain/exception/BusinessException.java` | Adicionar à `permits` |
| `db/migration/h2/V3__refresh_tokens.sql` | NEW |
| `db/migration/postgresql/V3__refresh_tokens.sql` | NEW |
| `application/service/RefreshTokenService.java` | NEW — create + refresh + rotation |
| `infrastructure/security/TokenAuthenticationService.java` | Adicionar `generateAccessToken()`, claim `token_type` |
| `infrastructure/security/JWTLoginFilter.java` | Retornar refresh token no body |
| `infrastructure/security/WebSecurityConfig.java` | `permitAll` em `/auth/refresh` |
| `infrastructure/security/LoginRateLimitFilter.java` | Estender para `/auth/refresh` |
| `ui/rest/controller/AuthController.java` | NEW — `POST /auth/refresh` |
| `ui/rest/controller/RestExceptionHandler.java` | Handler para `InvalidRefreshTokenException` |
| `domain/repository/UserRepository.java` | `findByIdWithAuthorities` |

## Data Model

Ver [data-model.md](./data-model.md).

## API Contracts

Ver [contracts/auth.yaml](./contracts/auth.yaml).

## Dependencies

Nenhuma nova dependência Maven.

## Migrations

- `V3__refresh_tokens.sql` (h2 + postgresql)
- PostgreSQL usa **BYTEA** para UUID (consistente com V1)
- FK `refresh_token.user_id → users(id)`

## Security Considerations

- **Token rotation:** uso marca `used=true`, gera novo. Mitigação contra reuso.
- **Re-validation:** refresh token re-checa `enabled` e `accountLocked` no
  momento do refresh (token velho de user bloqueado falha).
- **Rate limit:** estendido para `/auth/refresh` evita brute force de UUIDs.
- **Validation:** `@NotBlank` no `refreshToken` evita query com null.
- **Claim `token_type`:** distingue access de refresh (preparação para futura
  validação cruzada).

## Constitutional Compliance Checklist

- [x] Article I (Stack): novas classes usam idioms Java 21 (records para `RefreshRequest`/`TokenResponse`/`RefreshResult`, hierarquia sealed para `BusinessException`); migrations Flyway V3 separadas por vendor (`db/migration/h2/` e `db/migration/postgresql/`)
- [x] Article II (Layers): `UserDetailsAdapter` em `infrastructure/security/` desacopla `User` (domínio) de `UserDetails` (Spring Security) — `User.java` deixa de importar Spring Security (AC-11, AC-12, AC-13); `RefreshTokenService` em `application/service/`, controller fino em `ui/rest/controller/AuthController`
- [x] Article III (Records): `RefreshRequest`, `TokenResponse` e `RefreshResult` são `record`s imutáveis conforme exigência do artigo
- [x] Article IV (Exceptions): `InvalidRefreshTokenException` é `final extends BusinessException` e adicionada à cláusula `permits` da sealed class; `RestExceptionHandler` ganha handler dedicado retornando 401 com `ErrorBean`, sem expor stack trace
- [x] Article V (Security): rotation obrigatória — `used=true` no consumo invalida reuso (item 4, AC-3); claim `token_type` distingue access de refresh (item 3, AC-8); rate limit estendido a `/auth/refresh` (item 5, AC-9); `JWT_SECRET` via env var (item 2); BCrypt preservado (item 1); re-validação de `enabled` e `accountLocked` no refresh (AC-5, AC-6) reforça item 9 (soft-delete)
- [x] Article VI (Tests): unit tests para `RefreshTokenService` (rotation, expiração, revalidação) e integration tests para `POST /auth/refresh` cobrindo todos os cenários AC-1 a AC-10; suite verde com 193+ testes (AC-23)
- [x] Article VII (Persistence): `V3__refresh_tokens.sql` versionada e separada por vendor (item 1 e 5); `BYTEA` em PG / `UUID` em H2 mantém consistência com V1 (item 6); FK `refresh_token.user_id → users(id)`; auditoria automática herdada de `AbstractEntity` (item 4)
- [x] Article VIII (REST): `RefreshRequest` valida `@NotBlank` no campo `refreshToken` (AC-10); erros via `ErrorBean`; OpenAPI atualizado em `contracts/auth.yaml`; paginação não se aplica
- [x] Article IX (Config): `jwt.refresh-expiration` configurável (default 7 dias, AC-7); `DB_PASSWORD` e `JWT_SECRET` exigidos via fail-fast `${VAR:?}` em `docker-compose.yml` (AC-21); profiles dev/test/prod inalterados
- [x] Article X (CI/CD): pipeline existente do `maven.yml` cobre os novos testes (build, JaCoCo, OWASP) sem alteração estrutural
- [x] Article XI (Container): `Dockerfile` multi-stage com Maven JDK 21 → `eclipse-temurin:21-jre` (AC-17, itens 1 e 2); container roda como `non-root user` (AC-18, item 3); `${VAR:?}` garante fail-fast em `DB_PASSWORD` e `JWT_SECRET` (AC-21, item 4); PostgreSQL apenas em rede interna do compose, não publicado em prod (AC-22, item 5)
- [x] Article XII (Docs): README atualizado com fluxo de refresh token, comandos `docker compose`, variáveis de ambiente; coleção Postman sincronizada com `POST /auth/refresh` (item 4); `data-model.md` e `contracts/auth.yaml` documentam entidade e contrato

## Risks & Mitigations

| Risco | Mitigação |
|-------|-----------|
| T6 implementado em paralelo com T9 quebra | Sync obrigatório após T9; T6 pós-merge |
| Conflito em `TokenAuthenticationService` (T6 e T9) | Sequenciamento + resolução manual no merge |
| `JWTLoginFilter` `instanceof User` quebra após T9 | Refactor para `instanceof UserDetailsAdapter` |
| Migration V3 com tipo errado (UUID em PG vs BYTEA do V1) | Code review do tl-java capturou (CRITICAL C1) |
| `/auth/refresh` sem rate limit | Code review capturou (CRITICAL C2) — corrigido |
| `RefreshRequest.refreshToken` sem `@NotBlank` | Code review capturou (CRITICAL C3) — corrigido |
