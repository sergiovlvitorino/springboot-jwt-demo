# Tasks: Refresh Token & Containerization

**Plan:** [plan.md](./plan.md)
**Total tasks:** 3 (+ code review fixes)

## Execution Order

### Phase 1 — Parallel (independent)

- [x] **T13** — Dockerfile + docker-compose
  - **Agent:** @sre
  - **Files:** `Dockerfile` (new), `docker-compose.yml` (new), `docker-compose.dev.yml` (new), `.dockerignore` (new)
  - **Acceptance:** `docker compose up` sobe app + db; healthchecks passam
  - **Effort:** M

- [x] **T9** — UserDetailsAdapter (User desacoplado)
  - **Agent:** @dev-java
  - **Files:** `User.java`, `UserDetailsAdapter.java` (new), `UserService.java`, `TokenAuthenticationService.java`, testes
  - **Acceptance:** User não importa Spring Security; login continua funcionando
  - **Effort:** M

### CRITICAL SYNC

T9 DEVE estar 100% mergeado e testado (181+ testes verdes) antes de iniciar T6.
Verificar:
- `User.java` não tem `implements UserDetails`
- `UserDetailsAdapter.getUser()` funciona
- Login continua retornando 200

### Phase 2 — After T9 sync

- [x] **T6** — Refresh Token completo
  - **Agent:** @dev-java + @qa
  - **Files:** ver plan.md (15+ arquivos)
  - **Acceptance:** Todos os AC do spec.md (AC-1 a AC-10) passam; testes verdes
  - **Depends on:** T9 (princípio do JWTLoginFilter usa adapter)
  - **Effort:** G

### Phase 3 — Post code review (ad-hoc)

Após code review do `@tl-java`, 3 CRITICALs corrigidos:

- [x] **C1** — V3 PostgreSQL com BYTEA (consistência com V1)
- [x] **C2** — Rate limiter estendido para `/auth/refresh`
- [x] **C3** — `@NotBlank` + `@Valid` no `RefreshRequest`
- [x] **M5** — FK `refresh_token.user_id → users.id`
- [x] **M6** — Try-catch removido do `AuthController`, delegado ao `RestExceptionHandler`

## Parallelization

| Phase | Parallel | Sequential after |
|-------|----------|------------------|
| 1 | T13, T9 | sync (crítico) |
| 2 | T6 | code review |
| 3 | Fixes | (final) |

## Risk Tasks

- **T6:** A maior task do projeto (G). Requer mudanças em 15+ arquivos cruzando
  3 camadas. Riscos:
  - Conflito em `TokenAuthenticationService` (também tocado em T9)
  - `User.getUsername()` removido em T9 — refactor para `getEmail()`
  - `instanceof User` quebra — refactor para `instanceof UserDetailsAdapter`
  - Migration V3 com tipo errado em PG (capturado em code review)

## Follow-up (não implementado)

Major findings do code review pendentes para próximas sprints:
- **M1** — Cleanup automático de tokens expirados/usados
- **M2** — Substituir `UUID.randomUUID()` por `SecureRandom` 256-bit
- **M3** — Adicionar `@EnableScheduling` (rate limiter cleanup nunca executa)
- **M4** — `RefreshToken` extender `AbstractEntity` (audit trail)
- **M7** — Remover `setDateCreatedAt` manual no `UserService.save()`
- **M8** — Mascarar email em logs do `UserService`
