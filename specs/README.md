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
