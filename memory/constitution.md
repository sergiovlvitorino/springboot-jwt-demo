# Spring Boot JWT Demo Constitution

**Versão:** 1.0.0 | **Ratificado:** 2026-05-05 | **Última emenda:** 2026-05-06

> Esta constituição define princípios **invioláveis** do projeto. Toda spec, plan,
> task ou implementação deve respeitá-la. Violações exigem justificativa explícita
> e atualização desta constituição (com bump de versão semver).
>
> **Arquivo canônico.** Fiscalizado por `.github/workflows/sdd-gate.yml`.
> `.specify/memory/` é reservado à toolchain Spec Kit e não substitui este arquivo.

## Core Principles

### I. Modern Stack (NON-NEGOTIABLE)

Java **21** com idioms modernos (sealed classes, pattern matching, records,
sequenced collections); Spring Boot **3.5.x**; Maven como build tool;
Persistência H2 (dev/test) e PostgreSQL (prod); Migrations versionadas via
**Flyway**, separadas por vendor (`db/migration/h2/`, `db/migration/postgresql/`).

### II. Clean Layer Separation

Toda funcionalidade DEVE respeitar: `domain/` (entidades, repositórios,
exceções — **sem dependência de framework de apresentação ou segurança**) →
`application/` (services, commands, responses) → `infrastructure/` (segurança,
configurações, validações, integrações) → `ui/rest/controller/` (controllers,
exception handlers). Domínio NUNCA depende de Spring Security, MVC ou camadas
superiores.

### III. Immutability via Records

DTOs (commands, responses) DEVEM ser `record`. Entidades JPA são mutáveis
(exigência do framework), mas exposição externa só via record. Coleções
retornadas DEVEM ser imutáveis quando possível.

### IV. Sealed Exception Hierarchy

`BusinessException` é `abstract sealed class`. Subclasses DEVEM ser `final`.
Nova exceção de negócio exige adição à cláusula `permits`. `RestExceptionHandler`
tem handler dedicado por subclasse — **sem branch default genérico**.
Mensagens NUNCA expõem nomes de classe internos ou stack traces.

### V. Security by Default (NON-NEGOTIABLE)

Senhas SEMPRE encodadas via `PasswordEncoder` (BCrypt). JWT secret externalizado
via env var (`JWT_SECRET`), mínimo 32 caracteres. Tokens com claim `token_type`
para distinguir access/refresh. Refresh tokens com **rotation obrigatória**
(one-time use). Endpoints sensíveis (login, refresh) DEVEM ter rate limit.
Headers obrigatórios: HSTS, CSP, X-Frame-Options, X-Content-Type-Options,
Referrer-Policy. PII (emails) MASCARADO em logs. Authorities granulares —
sem promoção implícita. Soft-delete para entidades de negócio.

### VI. Test-First with Coverage Targets

Toda feature DEVE ter testes unitários **e** de integração. Validators
customizados exigem cobertura ≥ 90%. `@MockitoBean` (não `@MockBean` deprecated).
Testes de integração HTTP usam `@SpringBootTest(webEnvironment = RANDOM_PORT)` +
profile `test`. Testes de contrato via Spring Cloud Contract para endpoints
públicos. Build CI **deve falhar** se algum teste falhar.

### VII. Versioned Persistence

Schema DEVE ser versionado via Flyway, NUNCA via `ddl-auto=update` em prod.
`ddl-auto=validate` em produção. Migrations imutáveis após merge. Auditoria
(`dateCreatedAt`, `dateUpdatedAt`) automática via `@PrePersist`/`@PreUpdate`.
Seed (`Initialize.java`) restrito a `@Profile({"dev", "test"})` — NUNCA em prod.
UUID via `@JdbcTypeCode(java.sql.Types.BINARY)` — `BYTEA` em PostgreSQL,
`UUID` em H2.

### VIII. Stable REST Contracts

Controllers retornam DTOs (records), NUNCA entidades JPA. Validação via Bean
Validation (`@Valid`, `@NotBlank`, `@Max`, etc.) em todos os request bodies.
Paginação com limite máximo (`@Max(100)`). Endpoints públicos documentados em
OpenAPI (springdoc) — desabilitado em prod. Erros padronizados em `ErrorBean`
com `errorCode`, `field`, `message`.

### IX. Externalized Configuration

Properties sensíveis externalizadas via env vars. Profiles: `dev` (H2),
`test` (H2), `prod` (PostgreSQL). Defaults razoáveis em
`application.properties`, overrides por profile. Profile `test` ativado
automaticamente em `src/test/resources/application.properties`.

### X. Modern CI/CD

Pipeline em **GitHub Actions** (Travis CI proibido — substituído em 2026-04).
Etapas obrigatórias: build, test, JaCoCo report, Codecov upload, OWASP
dependency-check. Cobertura monitorada via Codecov. Vulnerabilidades bloqueiam
build em CVSS ≥ 7 (configurável).

### XI. Container-First Deploy

Dockerfile multi-stage (build + runtime separados). Imagem runtime:
`eclipse-temurin:21-jre`. Container roda com **non-root user**. Variáveis
sensíveis (DB_PASSWORD, JWT_SECRET) obrigatórias via fail-fast (`${VAR:?}`).
PostgreSQL **não exposto** em produção (apenas rede interna).

### XII. Persistent Specifications

Toda feature DEVE ter spec em `specs/NNN-feature-name/spec.md`. Toda decisão
arquitetural significativa DEVE ter ADR em `memory/decisions/` (raiz do projeto;
`.specify/memory/` é reservado à toolchain Spec Kit).
README atualizado a cada release/feature relevante. Postman collection mantida
sincronizada com endpoints públicos.

## Additional Constraints

### Technology Lock-ins
- Java version: 21 LTS
- Spring Boot: 3.5.x family
- Build: Maven (Gradle proibido para manter consistência)
- DB: H2 (dev/test) e PostgreSQL 16+ (prod)
- JWT lib: `nimbus-jose-jwt` via `spring-security-oauth2-jose`

### Security Requirements
- Min JWT secret: 32 caracteres
- Access token TTL default: 1h (configurável `jwt.expiration`)
- Refresh token TTL default: 7 dias (configurável `jwt.refresh-expiration`)
- Rate limit default: 10 tentativas/min (configurável `login.rate-limit.max-attempts`)
- BCrypt strength default

### Performance Standards
- Endpoints de listagem: `pageSize` ≤ 100
- Índices obrigatórios em colunas filtradas (`WHERE`, `ORDER BY`)

## Development Workflow

### Spec-Driven Development (Spec Kit)
1. **`/speckit-constitution`** (apenas em mudanças significativas)
2. **`/speckit-specify`** — cria spec da feature
3. **`/speckit-clarify`** (opcional) — resolve ambiguidades
4. **`/speckit-plan`** — cria plan técnico (valida contra esta constituição)
5. **`/speckit-tasks`** — quebra em tasks
6. **`/speckit-checklist`** (opcional) — checklist de qualidade
7. **`/speckit-analyze`** (opcional) — consistência cross-artifacts
8. **`/speckit-implement`** — executa

### Sub-Agents
| Tipo de task | Agente |
|--------------|--------|
| Código de produção | `@dev-java` (Sonnet) |
| Testes (unit, integration) | `@qa` (Sonnet) |
| CI/CD, Docker, infra, sealed | `@sre` (Opus) |
| Code review final | `@tl-java` (Opus) |
| Definição de produto | `@po` (Opus) |

### Quality Gates
- `mvn test` verde antes de merge
- Code review obrigatório por `@tl-java` em features G (grandes)
- 3 CRITICALs do code review bloqueiam merge
- `.github/workflows/sdd-gate.yml` valida invariantes SDD em PRs

## Governance

A constituição supersede todas outras práticas. Mudanças exigem:

1. PR dedicado com título `chore(constitution): ...`
2. Justificativa do impacto nas features existentes
3. Bump de versão semver na linha `**Versão:** X.Y.Z` (fiscalizado pelo SDD Gate)
4. Approval de no mínimo 1 Tech Lead

Toda PR e revisão DEVE verificar compliance com esta constituição. Complexidade
adicional DEVE ser justificada em `plan.md` da feature.
