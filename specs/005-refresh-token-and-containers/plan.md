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

- [x] Article I: Java 21 idioms (records, sealed)
- [x] Article II: adapter em infra, domínio limpo
- [x] Article III: DTOs como record
- [x] Article IV: `InvalidRefreshTokenException` final, na `permits`
- [x] Article V: rotation, claim, rate limit, validação, encoding senha
- [x] Article VI: unit + integration tests para refresh token
- [x] Article VII: migration vendor-specific, FK, audit
- [x] Article VIII: validation no DTO, paginação N/A
- [x] Article IX: env var `JWT_REFRESH_EXPIRATION`
- [x] Article X: CI cobre
- [x] Article XI: multi-stage, non-root, fail-fast vars
- [x] Article XII: README + Postman atualizados

## Risks & Mitigations

| Risco | Mitigação |
|-------|-----------|
| T6 implementado em paralelo com T9 quebra | Sync obrigatório após T9; T6 pós-merge |
| Conflito em `TokenAuthenticationService` (T6 e T9) | Sequenciamento + resolução manual no merge |
| `JWTLoginFilter` `instanceof User` quebra após T9 | Refactor para `instanceof UserDetailsAdapter` |
| Migration V3 com tipo errado (UUID em PG vs BYTEA do V1) | Code review do tl-java capturou (CRITICAL C1) |
| `/auth/refresh` sem rate limit | Code review capturou (CRITICAL C2) — corrigido |
| `RefreshRequest.refreshToken` sem `@NotBlank` | Code review capturou (CRITICAL C3) — corrigido |
