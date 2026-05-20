# Feature: Database Migrations & Performance

**Status:** Implemented
**Author:** sergio
**Created:** 2026-04-13
**Spec ID:** 003
**Sprint:** 3

## Problem Statement

O projeto não tinha gestão de schema versionada — produção dependia de criação
manual de tabelas. Auditoria de `dateUpdatedAt` era manual em cada service
(propenso a erro). Listagens filtravam por colunas sem índice, causando full
table scans em volume.

Além disso, o seed `Initialize.java` rodava em **todos os profiles**, incluindo
prod — risco crítico de criar admin com senha hardcoded em produção.

## User Stories

- Como **operador de banco**, quero migrations versionadas, para reproduzir o
  schema em qualquer ambiente sem intervenção manual.
- Como **operador de produção**, quero garantia de que dados de seed (admin com
  senha conhecida) NUNCA sejam criados em prod, para evitar vetor de ataque.
- Como **DBA**, quero índices nas colunas filtradas, para que listagens escalem
  com o volume da tabela.
- Como **mantenedor**, quero auditoria de `dateUpdatedAt` automática, para não
  depender de cada service lembrar de setar.

## Acceptance Criteria

- [AC-1] Flyway está como dependência no `pom.xml`
- [AC-2] Migration `V1__baseline.sql` cria todas as tabelas existentes
- [AC-3] Migrations separadas por vendor: `db/migration/h2/` e `db/migration/postgresql/`
- [AC-4] PostgreSQL usa `BYTEA` para UUIDs; H2 usa `UUID` nativo
- [AC-5] Migration `V2__add_indexes.sql` cria índices em `users.enabled`,
        `users.name`, `role.name`
- [AC-6] `Initialize.java` tem `@Profile({"dev", "test"})` — não roda em prod
- [AC-7] `AbstractEntity.onPreUpdate()` seta `dateUpdatedAt` automaticamente
- [AC-8] `UserService.update()` NÃO faz `setDateUpdatedAt()` manual
- [AC-9] Profile prod usa `ddl-auto=validate` (Flyway cria, Hibernate valida)
- [AC-10] Suite de testes verde

## Out of Scope

- Migration de dados (apenas schema)
- Particionamento de tabelas
- Read replicas
- Cleanup automático de soft-deleted records

## Open Questions

- ~~`ddl-auto` em dev/test: validate ou create-drop?~~ → validate (paridade com prod)

## Constitutional Compliance

- ✓ Article VII (Persistence): Flyway, validate em prod, audit automático,
  seed restrito a dev/test, UUID com `@JdbcTypeCode(BINARY)`
