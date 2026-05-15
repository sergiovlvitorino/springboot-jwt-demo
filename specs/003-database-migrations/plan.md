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

- [x] Article I (Stack): Flyway introduzido como migration tool oficial (item 5) com layout vendor-specific `db/migration/h2/` e `db/migration/postgresql/`; bancos H2 (dev/test) e PostgreSQL (prod) confirmados
- [x] Article II (Layers): migrations vivem em `src/main/resources/db/migration/`, auditoria em `domain/model/AbstractEntity`, seed em `infrastructure/Initialize`; sem mistura de camadas
- [x] Article III (Records): N/A — feature trata schema e auditoria, sem novos DTOs
- [x] Article IV (Exceptions): N/A — sem novas exceções de negócio
- [x] Article V (Security): item 9 (soft-delete) preservado; risco crítico mitigado ao restringir `Initialize.java` a `@Profile({"dev","test"})`, evitando criação de admin com senha hardcoded em prod (AC-6)
- [x] Article VI (Tests): suite verde após migrations (AC-10); testes de integração validam que `ddl-auto=validate` aceita o schema produzido pelo Flyway em ambos os vendors
- [x] Article VII (Persistence): cobre TODOS os itens — Flyway versionado (item 1), `ddl-auto=validate` em prod (item 2, AC-9), migrations imutáveis (item 3), auditoria automática via `@PreUpdate` em `AbstractEntity.onPreUpdate()` (item 4, AC-7/AC-8), seed restrito a dev/test (item 5, AC-6), UUID BINARY com `BYTEA` em PG e `UUID` em H2 (item 6, AC-4)
- [x] Article VIII (REST): N/A — sem mudança de contrato externo
- [x] Article IX (Config): `application-dev.properties`, `application-test.properties` e `application-prod.properties` configuram Flyway location e `ddl-auto` por profile, alinhados aos profiles dev/test/prod (itens 2 e 3)
- [x] Article X (CI/CD): N/A — sem mudança de pipeline (testes existentes validam migrations)
- [x] Article XI (Container): N/A — containerização entregue em Spec 005, mas migrations preparam o terreno para o profile prod containerizado
- [x] Article XII (Docs): README atualizado documentando estratégia de migrations, layout vendor-specific e comportamento do seed por profile

## Risks & Mitigations

| Risco | Mitigação |
|-------|-----------|
| Schema validation falha em testes | UUID type vs BINARY descoberto e corrigido (BYTEA em PG, UUID em H2) |
| `cors.allowed-origins` perdido em test profile | Adicionado em `application-test.properties` |
| Spring context cache key muda com novas properties | Suite valida |
| Migrations não-idempotentes | Flyway garante execução única por versão |
