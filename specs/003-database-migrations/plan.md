# Implementation Plan: Database Migrations & Performance

**Spec:** [spec.md](./spec.md)
**Status:** Implemented

## Architecture

T3 (Flyway baseline) é **fundacional** — bloqueia T4 (que precisa que V1 exista
para adicionar V2). T10 é independente.

```
Phase 1:
  ├── @dev-java   → Flyway baseline + @Profile (T3) ──┐
  └── @qa         → Plano de testes para migration    │
                                                      ▼ sync
Phase 2 (após T3):
  ├── @dev-java   → V2 indexes (T4)
  └── @qa         → @PreUpdate audit (T10)
```

## Components

| Componente | Tipo | Mudança |
|------------|------|---------|
| `pom.xml` | Build | `flyway-core` + `flyway-database-postgresql` |
| `src/main/resources/db/migration/h2/V1__baseline.sql` | Migration | Schema H2 |
| `src/main/resources/db/migration/postgresql/V1__baseline.sql` | Migration | Schema PG |
| `src/main/resources/db/migration/h2/V2__add_indexes.sql` | Migration | Índices |
| `src/main/resources/db/migration/postgresql/V2__add_indexes.sql` | Migration | Índices |
| `application-dev.properties` | Config | Flyway H2 location, validate |
| `application-test.properties` | Config | Flyway H2 location, validate |
| `application-prod.properties` | Config | Flyway PG location, validate |
| `infrastructure/Initialize.java` | Seed | `@Profile({"dev", "test"})` |
| `domain/model/AbstractEntity.java` | Entity | `@PreUpdate onPreUpdate()` |
| `application/service/UserService.java` | Service | Remover `setDateUpdatedAt()` manual |

## Data Model

Schema baseline (extraído das entidades JPA):

```
authority           (id BINARY/UUID, name VARCHAR)
role                (id BINARY/UUID, name VARCHAR)
role_authority      (role_id, authority_id)  [join table]
users               (id, name, email UNIQUE, password, enabled,
                     account_locked, role_id FK, audit fields)
```

Índices (V2):
```sql
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_users_name    ON users(name);
CREATE INDEX idx_role_name     ON role(name);
```

## API Contracts

Sem mudança de contrato externo.

## Dependencies

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

## Migrations

- `V1__baseline.sql` (h2 + postgresql)
- `V2__add_indexes.sql` (h2 + postgresql)

## Security Considerations

- **CRÍTICO:** `Initialize.java` sem `@Profile` rodaria em prod, criando admin
  com senha `Test@1234`. Restrição a dev/test mitiga.
- `ddl-auto=validate` em prod previne mutação acidental de schema.

## Constitutional Compliance Checklist

- [x] Article I: N/A
- [x] Article II: respeitado
- [x] Article III: N/A
- [x] Article IV: N/A
- [x] Article V: seed restrito a dev/test
- [x] Article VI: testes validam migrations
- [x] Article VII: Flyway, validate, vendor-specific, BINARY UUID, `@PreUpdate`
- [x] Article VIII: N/A
- [x] Article IX: profiles configurados
- [x] Article X: N/A
- [x] Article XI: N/A
- [x] Article XII: README atualizado

## Risks & Mitigations

| Risco | Mitigação |
|-------|-----------|
| Schema validation falha em testes | UUID type vs BINARY descoberto e corrigido (BYTEA em PG, UUID em H2) |
| `cors.allowed-origins` perdido em test profile | Adicionado em `application-test.properties` |
| Spring context cache key muda com novas properties | Suite valida |
| Migrations não-idempotentes | Flyway garante execução única por versão |
