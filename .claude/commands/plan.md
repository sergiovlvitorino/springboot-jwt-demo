---
description: Cria plan.md (COMO técnico) a partir de uma spec existente.
---

# /plan

Você está criando um **implementation plan** baseado em uma spec existente.

## Contexto

Leia primeiro:
- `memory/constitution.md` — princípios invioláveis
- `specs/NNN-feature-name/spec.md` — a spec da feature
- Código existente relevante (use Grep/Read para descobrir)

## Sua tarefa

Produzir `specs/NNN-feature-name/plan.md` + arquivos auxiliares conforme necessário:
- `specs/NNN-feature-name/data-model.md` (entidades, schemas, migrations)
- `specs/NNN-feature-name/contracts/` (OpenAPI yaml ou JSON Schema)
- `specs/NNN-feature-name/research.md` (trade-offs investigados)
- `specs/NNN-feature-name/quickstart.md` (como testar manualmente)

## Estrutura obrigatória do plan.md

```markdown
# Implementation Plan: <Nome>

**Spec:** [spec.md](./spec.md)
**Status:** Draft

## Architecture
<Decisões arquiteturais — diagramas ASCII se ajudar>

## Components
- <Lista de componentes novos/modificados, com responsabilidade>

## Data Model
<Resumo. Detalhes em data-model.md>

## API Contracts
<Resumo. Detalhes em contracts/>

## Dependencies
- <Bibliotecas novas no pom.xml, se houver, com justificativa>

## Migrations
- <Lista de migrations Flyway necessárias>

## Security Considerations
<Authorities, validações, rate limit, sanitização>

## Constitutional Compliance Checklist
- [ ] Article I (Stack): respeitado
- [ ] Article II (Layers): sem domínio dependendo de framework
- [ ] Article III (Records): DTOs como record
- [ ] Article IV (Exceptions): sealed + final + permits
- [ ] Article V (Security): senha, JWT, rate limit, headers, PII
- [ ] Article VI (Tests): unit + integration planejados
- [ ] Article VII (Persistence): Flyway, validate, audit
- [ ] Article VIII (REST): DTOs, validation, paginação, OpenAPI
- [ ] Article IX (Config): env vars, profiles
- [ ] Article X (CI/CD): pipeline cobre
- [ ] Article XI (Container): se aplicável
- [ ] Article XII (Docs): planejado

## Risks & Mitigations
- <Risco>: <mitigação>
```

## Regras

1. **OBRIGATÓRIO** validar TODA a constitution checklist
2. Se algum item violar, **PARE** e justifique antes de continuar
3. Reuse padrões existentes — não invente novos sem necessidade
4. Decisões controversas vão em `research.md` com alternativas analisadas
5. Após criar, sugira `/tasks` para quebra em itens executáveis
