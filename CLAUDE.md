# Onboarding — Spring Boot JWT Demo

## Project overview

Aplicação Spring Boot **3.5** rodando em **Java 21**, com autenticação/autorização baseada em **JWT**
(access token + refresh token com rotation). Persistência via JPA/Hibernate, com **H2** em
`dev`/`test` e **PostgreSQL** em `prod`. Schema versionado por **Flyway**, dois diretórios separados
por vendor (`db/migration/h2/`, `db/migration/postgresql/`). Demo de autenticação granular com
authorities por endpoint, headers de segurança, rate limit e PII mascarada em logs.

## Leituras obrigatórias antes de qualquer trabalho

1. `memory/constitution.md` — princípios **invioláveis** (12 artigos: stack, camadas,
   imutabilidade, exceções, segurança, testes, persistência, API, configuração, CI/CD,
   containerização, documentação). Toda spec/plan/task DEVE respeitar.
2. `specs/README.md` — índice de features SDD (`001` a `005`, com status).
3. `memory/decisions/` — ADRs do projeto. Antes de propor decisão arquitetural significativa,
   verifique se já existe ADR sobre o tema.

Sem ler esses três artefatos, não inicie modificações.

## Ciclo SDD (Spec-Driven Development)

Comandos do Spec Kit, na ordem natural:

| Comando | Função |
|---------|--------|
| `/specify` | Cria `spec.md` (O QUÊ e POR QUÊ, sem como) |
| `/clarify` | Resolve ambiguidades (opcional) |
| `/plan` | Cria `plan.md` (COMO técnico) |
| `/tasks` | Decompõe em tarefas executáveis |
| `/analyze` | Valida consistência spec/plan/tasks (opcional) |
| `/implement` | Executa as tarefas |
| `/constitution` | Edita `memory/constitution.md` (exige PR dedicado e bump de versão) |

## Convenções de spec

- **ID** zero-padded a 3 dígitos: `001`, `002`, ..., kebab-case no nome (`005-refresh-token-and-containers`).
- **Status**: `Draft` | `Clarifying` | `Planned` | `In Progress` | `Implemented` | `Deprecated`.
- **Acceptance Criteria** verificáveis, marcados com tag `[AC-1]`, `[AC-2]` ... — cada AC tem
  resultado passa/falha objetivo.
- **`Out of Scope`** explícito é tão importante quanto o escopo.
- Estrutura de diretório: `specs/NNN-feature-name/{spec.md, plan.md, tasks.md, [data-model.md],
  [contracts/], [quickstart.md], [research.md]}`.

## Stack & comandos úteis

```bash
./mvnw clean verify              # build + testes + JaCoCo + OWASP dep-check
./mvnw spring-boot:run           # roda local (default profile = dev, H2)
./mvnw test                      # apenas testes
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
docker compose up --build        # sobe Postgres + app (precisa DB_PASSWORD, JWT_SECRET)
docker compose -f docker-compose.dev.yml up   # apenas Postgres para dev local
```

Profiles: **`dev`** (H2 em memória, seed via `Initialize.java`), **`test`** (H2, ativo automaticamente
em `src/test/resources/application.properties`), **`prod`** (PostgreSQL, OpenAPI desabilitado,
`ddl-auto=validate`).

## Estrutura de camadas

| Camada | Pacote | Regras |
|--------|--------|--------|
| Domain | `domain/` | Entidades JPA, repositórios, exceções de negócio. **Sem** Spring Security/MVC |
| Application | `application/` | Services + commands (input DTOs) + responses (output DTOs) |
| Infrastructure | `infrastructure/` | Security, configs, validators, integrações |
| UI | `ui/rest/controller/` | Controllers REST + `RestExceptionHandler` |

Domínio NUNCA depende de camadas superiores. Acoplamento entre camadas só ocorre por interfaces
do domínio (ex.: `UserRepository`).

## Regras de ouro (extraídas da constituição)

- **DTOs como `record`** (commands e responses). Entidades JPA NUNCA expostas no controller.
- **`BusinessException` é `sealed`**; subclasses `final`; nova exceção exige adição em `permits` E
  novo handler em `RestExceptionHandler` (sem branch default genérico para exceções de negócio).
  Ver ADR-0001.
- **PII mascarada** em logs (`maskEmail()`); nunca logar senha, token, refresh token.
- **`PasswordEncoder` (BCrypt)** sempre — nunca persistir senha em texto plano.
- **Flyway versionado**, migrations imutáveis após merge; `ddl-auto=validate` em prod.
- **`@MockitoBean`** ao invés de `@MockBean` (deprecated).
- **Rate limit obrigatório** em endpoints sensíveis (`/login`, `/refresh`).
- **Authorities granulares** (`USER_DELETE` ≠ `USER_SAVE`); soft-delete via `enabled=false`.
- **Refresh token one-time use** com rotation (ADR-0003).
- **Container non-root**, secrets via env var com fail-fast `${VAR:?}` (ADR-0004).
- **Testes**: unit + integration para toda feature; cobertura ≥ 90% em validators custom.

## Política de PR

- **Sem spec aprovada (`Implemented`-ready), sem PR de feature.** PRs de feature SEM spec
  associada serão rejeitados.
- Mudanças na constituição: PR dedicado `chore(constitution): ...` + justificativa de impacto +
  bump de versão semver + approval de Tech Lead.
- ADRs novos para decisões arquiteturais significativas (ver `memory/decisions/README.md`).

<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read the current plan
<!-- SPECKIT END -->
