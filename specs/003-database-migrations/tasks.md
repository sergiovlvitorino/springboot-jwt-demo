# Tasks: Database Migrations & Performance

**Plan:** [plan.md](./plan.md)
**Total tasks:** 3

## Execution Order

### Phase 1 — Foundation

- [x] **T3** — Flyway baseline + Initialize @Profile
  - **Agent:** @dev-java
  - **Files:** `pom.xml`, `db/migration/h2/V1__baseline.sql`, `db/migration/postgresql/V1__baseline.sql`, `application-{dev,test,prod}.properties`, `Initialize.java`
  - **Acceptance:** `mvn test` verde com Flyway ativo; seed não roda em prod
  - **Effort:** M

### Phase 2 — Parallel (after T3 merged)

- [x] **T4** — V2 indexes
  - **Agent:** @dev-java
  - **Files:** `db/migration/h2/V2__add_indexes.sql` (new), `db/migration/postgresql/V2__add_indexes.sql` (new)
  - **Acceptance:** Migration executa em ordem; suite verde
  - **Depends on:** T3
  - **Effort:** P

- [x] **T10** — `@PreUpdate` automático
  - **Agent:** @qa
  - **Files:** `AbstractEntity.java`, `UserService.java`, `AbstractEntityTest.java`, `PreUpdateAuditTest.java` (new)
  - **Acceptance:** `dateUpdatedAt` populado sem chamada manual; testes validam
  - **Effort:** P

## Parallelization

| Phase | Parallel | Sequential after |
|-------|----------|------------------|
| 1 | T3 | sync (crítico) |
| 2 | T4, T10 | (final) |

## Risk Tasks

- **T3:** Configuração de Flyway por vendor é não-trivial. UUID type mismatch
  causou 50 erros de teste — corrigido na integração (BYTEA em PG, UUID em H2).
