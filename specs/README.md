# Specs

Especificações de features seguindo **Spec-Driven Development** (modelo Spec Kit).

## Como usar

### Para uma nova feature

```
/specify  → cria spec.md (O QUÊ e POR QUÊ, sem como)
/clarify  → resolve ambiguidades (opcional)
/plan     → cria plan.md (COMO técnico)
/tasks    → quebra em tasks executáveis
/analyze  → valida consistência (opcional)
/implement → executa
```

### Para entender uma feature existente

Cada diretório `NNN-feature-name/` contém:

- `spec.md` — **fonte da verdade** sobre o que a feature faz
- `plan.md` — como foi/será implementada
- `tasks.md` — decomposição executável (com `[x]` para concluídas)
- `data-model.md` (opcional) — schemas, entidades
- `contracts/` (opcional) — OpenAPI, JSON Schema
- `quickstart.md` (opcional) — como testar manualmente
- `research.md` (opcional) — trade-offs investigados

## Features

| ID | Feature | Sprint | Status |
|----|---------|--------|--------|
| [001](./001-modernization-foundation/) | Modernization Foundation | 1 | Implemented |
| [002](./002-security-hardening/) | Security Hardening | 2 | Implemented |
| [003](./003-database-migrations/) | Database Migrations & Performance | 3 | Implemented |
| [004](./004-quality-improvements/) | Quality Improvements | 4 | Implemented |
| [005](./005-refresh-token-and-containers/) | Refresh Token & Containerization | 5 | Implemented |

## Constituição

Antes de criar uma nova spec, leia `memory/constitution.md` — define princípios
invioláveis que toda spec deve respeitar.

## Convenções

- Spec ID é zero-padded a 3 dígitos (`001`, `002`, ...)
- Feature name é kebab-case
- Status: `Draft`, `Clarifying`, `Planned`, `In Progress`, `Implemented`, `Deprecated`
- ACs verificáveis (passa/falha objetivo)
- `Out of Scope` explícito é tão importante quanto o escopo

## Política de artefatos

A tríade `spec.md`, `plan.md` e `tasks.md` é **obrigatória** para toda feature
(Article XII da constituição). Os demais artefatos são condicionais à natureza
da feature, conforme a matriz abaixo.

### Matriz de artefatos por tipo de feature

| Tipo de feature | spec.md | plan.md | tasks.md | data-model.md | contracts/ | quickstart.md | research.md |
|---|---|---|---|---|---|---|---|
| Cria/altera schema ou entidade JPA | obrigatório | obrigatório | obrigatório | **obrigatório** | se há endpoint público | recomendado | se houver trade-off |
| Cria/altera endpoint REST público | obrigatório | obrigatório | obrigatório | se toca persistência | **obrigatório** (OpenAPI YAML) | recomendado | se houver trade-off |
| Fluxo manual não-trivial (login, container, setup) | obrigatório | obrigatório | obrigatório | se aplicável | se aplicável | **obrigatório** | opcional |
| Decisão técnica não-óbvia (ex.: escolha entre 2 libs) | obrigatório | obrigatório | obrigatório | se aplicável | se aplicável | opcional | **obrigatório** |
| Refactor puro (sem novo comportamento externo) | obrigatório | obrigatório | obrigatório | não | não | não | opcional |
| Hardening/Operacional sem novo endpoint | obrigatório | obrigatório | obrigatório | não | não | recomendado | opcional |

### Regras de decisão

- Se a feature **cria ou altera schema/entidade JPA**, `data-model.md` é obrigatório
- Se a feature **cria ou altera endpoint REST público**, `contracts/` (OpenAPI YAML) é obrigatório
- Se a feature tem **fluxo manual não-trivial** (login flow, container setup, smoke test), `quickstart.md` é obrigatório
- Se a feature envolve **decisão técnica não-óbvia** (ex.: escolha entre 2 libs, algoritmo, formato), `research.md` é obrigatório (e considere também ADR em `memory/decisions/`)
- Se a feature é **refactor puro** (preserva comportamento externo), apenas a tríade é exigida

### Exemplos retroativos

Aplicando a política às specs existentes (sem refatorar — apenas registro de débito):

- **Spec 001 — Modernization Foundation:** upgrade de versões, sem novo endpoint nem schema. Tríade suficiente. Conforme.
- **Spec 002 — Security Hardening:** novo authority `USER_DELETE` e headers CSP/HSTS afetam contrato de respostas. Débito: `contracts/` documentando headers e respostas 403 seria recomendável.
- **Spec 003 — Database Migrations:** cria a estrutura de migrations Flyway vendor-specific (V1, V2). Débito: `data-model.md` seria obrigatório pela política atual; o schema vive apenas nos arquivos `db/migration/{h2,postgresql}/`.
- **Spec 004 — Quality Improvements:** refactor + testes, sem novo endpoint nem schema. Tríade suficiente. Conforme.
- **Spec 005 — Refresh Token & Containerization:** novo endpoint `POST /auth/refresh`, nova entidade `RefreshToken`, fluxo manual de container e decisão UUID vs `SecureRandom`. Possui `data-model.md`, `contracts/auth.yaml` e `quickstart.md`. Conforme (a decisão UUID vs `SecureRandom` está registrada nas Open Questions da própria spec, em vez de `research.md`).

> Specs marcadas como débito **não** serão refatoradas retroativamente.
> A política vale para novas specs a partir desta versão.
