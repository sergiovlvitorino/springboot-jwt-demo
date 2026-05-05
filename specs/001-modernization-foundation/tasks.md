# Tasks: Modernization Foundation

**Plan:** [plan.md](./plan.md)
**Total tasks:** 4

## Execution Order

### Phase 1 — Independent (parallel)

- [x] **T1** — Remover Travis CI, atualizar GitHub Actions
  - **Agent:** @sre
  - **Files:** `.travis.yml` (delete), `system.properties`, `.github/workflows/maven.yml`
  - **Acceptance:** CI verde, JaCoCo report uploaded, Codecov action presente
  - **Effort:** P

- [x] **T2** — Criar `RoleResponse` record
  - **Agent:** @dev-java
  - **Files:** `application/command/role/RoleResponse.java` (new), `ui/rest/controller/RoleRestController.java`, `RoleRestControllerTest.java`
  - **Acceptance:** `mvn test` verde; controller retorna `Page<RoleResponse>`
  - **Effort:** P

- [x] **T12** — Migrar `@MockBean` → `@MockitoBean`
  - **Agent:** @qa
  - **Files:** `contract/BaseContractTest.java`
  - **Acceptance:** Nenhuma ocorrência de `@MockBean` em src/test; testes verdes
  - **Effort:** P

### Phase 2 — After Phase 1 sync

- [x] **T11** — Limitar `pageSize` máximo
  - **Agent:** @dev-java
  - **Files:** `application/command/user/ListCommand.java`, `application/command/role/ListCommand.java`, testes correspondentes
  - **Acceptance:** Request com `pageSize=101` retorna 400; suite verde
  - **Depends on:** T2 (mesmos pacotes)
  - **Effort:** P

## Parallelization

| Phase | Parallel | Sequential after |
|-------|----------|------------------|
| 1 | T1, T2, T12 | sync |
| 2 | T11 | (final) |

## Risk Tasks

- **T1:** Codecov pode falhar silenciosamente se `CODECOV_TOKEN` não estiver
  configurado — usar `fail_ci_if_error: false`
