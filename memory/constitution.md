# Project Constitution — Spring Boot JWT Demo

> Esta constituição define princípios **invioláveis** do projeto. Toda spec, plan,
> task ou implementação deve respeitá-la. Violações exigem justificativa explícita
> e atualização desta constituição.

**Versão:** 1.0.0 — 2026-05-05
**Ratificado em:** branch `chore/java21-modernization-and-deps`

---

## Article I — Stack Tecnológica

1. Java **21** (sealed classes, pattern matching, records, sequenced collections)
2. Spring Boot **3.5.x**
3. Build com Maven
4. Persistência: H2 (dev/test) e PostgreSQL (prod)
5. Migrations versionadas via **Flyway**, separadas por vendor (`db/migration/h2/`, `db/migration/postgresql/`)

## Article II — Arquitetura em Camadas

Toda funcionalidade DEVE respeitar a separação:

- `domain/` — entidades, repositórios, exceções de negócio. **Sem dependência de framework de apresentação ou segurança.**
- `application/` — services, commands (input DTOs), responses (output DTOs)
- `infrastructure/` — segurança, configurações, validações, integrações
- `ui/rest/controller/` — controllers REST, exception handlers

Domínio NUNCA depende de Spring Security, MVC ou camadas superiores.

## Article III — Imutabilidade e Records

1. DTOs (commands, responses) DEVEM ser `record`
2. Entidades JPA são mutáveis (exigência do framework), mas exposição externa só via `record`
3. Coleções retornadas DEVEM ser imutáveis quando possível

## Article IV — Tratamento de Exceções

1. Hierarquia de exceções de negócio é uma **sealed class** (`BusinessException`)
2. Subclasses DEVEM ser `final`
3. Nova exceção de negócio exige adição à cláusula `permits`
4. Handler em `RestExceptionHandler` por subclasse — **sem branch default genérico**
5. Mensagens de erro NUNCA expõem nomes de classe internos ou stack traces

## Article V — Segurança

1. Senhas SEMPRE encodadas via `PasswordEncoder` (BCrypt)
2. JWT secret externalizado via env var (`JWT_SECRET`), mínimo 32 caracteres
3. Tokens DEVEM ter claim `token_type` para distinguir access/refresh
4. Refresh tokens com **rotation obrigatória** (one-time use)
5. Endpoints sensíveis DEVEM ter rate limit (login, refresh)
6. Headers de segurança obrigatórios: HSTS, CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy
7. PII (emails) MASCARADO em logs (`maskEmail()`)
8. Authorities granulares (`USER_DELETE` ≠ `USER_SAVE`) — sem promoção implícita
9. Soft-delete para entidades de negócio (`enabled=false`), nunca hard delete

## Article VI — Testes

1. Toda feature DEVE ter testes unitários **e** de integração
2. Validators customizados exigem cobertura ≥ 90%
3. `@MockitoBean` ao invés de `@MockBean` (deprecated)
4. Testes de integração HTTP usam `@SpringBootTest(webEnvironment = RANDOM_PORT)` + profile `test`
5. Testes de contrato via Spring Cloud Contract para endpoints públicos
6. Build CI **deve falhar** se algum teste falhar

## Article VII — Persistência

1. Schema DEVE ser versionado via Flyway, NUNCA via `ddl-auto=update` em prod
2. `ddl-auto=validate` em produção (Flyway cria, Hibernate valida)
3. Migrations imutáveis após merge — nova mudança = nova versão
4. Auditoria (`dateCreatedAt`, `dateUpdatedAt`) automática via `@PrePersist` / `@PreUpdate`
5. Seed data (`Initialize.java`) restrito a `@Profile({"dev", "test"})` — NUNCA em prod
6. UUID via `@JdbcTypeCode(java.sql.Types.BINARY)` — `BYTEA` em PostgreSQL, `UUID` em H2

## Article VIII — API REST

1. Controllers retornam DTOs (records), NUNCA entidades JPA
2. Validação via Bean Validation (`@Valid`, `@NotBlank`, `@Max`, etc.) em todos os request bodies
3. Paginação com limite máximo (`@Max(100)` no `pageSize`)
4. Endpoints públicos documentados em OpenAPI (springdoc) — desabilitado em prod
5. Erros padronizados em `ErrorBean` com `errorCode`, `field`, `message`

## Article IX — Configuração

1. Properties sensíveis externalizadas via env vars
2. Profiles: `dev` (H2), `test` (H2), `prod` (PostgreSQL)
3. Defaults razoáveis em `application.properties`, overrides por profile
4. Profile `test` ativado automaticamente em `src/test/resources/application.properties`

## Article X — CI/CD

1. Pipeline em GitHub Actions (Travis CI proibido)
2. Etapas obrigatórias: build, test, JaCoCo report, Codecov upload, OWASP dependency-check
3. Cobertura monitorada via Codecov
4. Vulnerabilidades bloqueiam build em CVSS ≥ 7 (configurável)

## Article XI — Containerização

1. Dockerfile multi-stage (build + runtime separados)
2. Imagem runtime: `eclipse-temurin:21-jre`
3. Container roda com **non-root user**
4. Variáveis sensíveis (DB_PASSWORD, JWT_SECRET) obrigatórias via fail-fast (`${VAR:?}`)
5. PostgreSQL **não exposto** em produção (apenas rede interna)

## Article XII — Documentação

1. Toda feature DEVE ter spec em `specs/NNN-feature-name/spec.md`
2. Toda decisão arquitetural significativa DEVE ter ADR em `memory/decisions/`
3. README atualizado a cada release/feature relevante
4. Postman collection mantida sincronizada com endpoints públicos

---

## Governança

Mudanças nesta constituição exigem:
1. PR dedicado com título `chore(constitution): ...`
2. Justificativa do impacto nas features existentes
3. Bump de versão (semver: BREAKING/MINOR/PATCH)
4. Approval de no mínimo 1 Tech Lead
