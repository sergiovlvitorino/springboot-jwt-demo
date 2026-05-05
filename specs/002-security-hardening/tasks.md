# Tasks: Security Hardening

**Plan:** [plan.md](./plan.md)
**Total tasks:** 4

## Execution Order

### Phase 1 — Parallel

- [x] **T18** — OWASP dependency-check no CI
  - **Agent:** @sre
  - **Files:** `pom.xml`, `dependency-check-suppressions.xml` (new), `.github/workflows/maven.yml`
  - **Acceptance:** Job separado roda `mvn dependency-check:check`; relatório como artefato
  - **Effort:** P

- [x] **T5** — Authorities granulares (`USER_DELETE`)
  - **Agent:** @dev-java
  - **Files:** `AuthorityConstants.java`, `Initialize.java`, `Config.java` (`@EnableMethodSecurity`), `UserRestController.java`, `RestExceptionHandler.java`, testes
  - **Acceptance:** GUEST sem `USER_DELETE` recebe 403; suite verde
  - **Effort:** P

- [x] **T7** — `X-Forwarded-For` no rate limiter
  - **Agent:** @qa
  - **Files:** `LoginRateLimitFilter.java`, `LoginRateLimitFilterTest.java`
  - **Acceptance:** 4 cenários de XFF testados; suite verde
  - **Effort:** M

### Phase 2 — Sequential (after Phase 1 merge)

- [x] **T14** — CSP + HSTS headers
  - **Agent:** @dev-java
  - **Files:** `WebSecurityConfig.java`, `SecurityHeadersTest.java` (new)
  - **Acceptance:** Resposta HTTP contém ambos os headers; teste de integração valida
  - **Depends on:** T5 (mesmo arquivo `WebSecurityConfig`)
  - **Effort:** P

## Parallelization

| Phase | Parallel | Sequential after |
|-------|----------|------------------|
| 1 | T18, T5, T7 | sync |
| 2 | T14 | (final) |

## Risk Tasks

- **T5:** Descobriu bug latente (`@EnableMethodSecurity` ausente). Antes desta
  task, **TODOS** os `@PreAuthorize` eram silenciosamente ignorados.
- **T14:** Conflito potencial com T5 em `WebSecurityConfig`. Sequenciamento
  obrigatório resolveu.
