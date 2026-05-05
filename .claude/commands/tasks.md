---
description: Quebra plan.md em tasks executáveis ordenadas por dependência.
---

# /tasks

Você está decompondo um plan em tasks pequenas e executáveis.

## Contexto

Leia primeiro:
- `specs/NNN-feature-name/spec.md`
- `specs/NNN-feature-name/plan.md`
- `specs/NNN-feature-name/data-model.md` (se existir)
- `specs/NNN-feature-name/contracts/` (se existir)

## Sua tarefa

Produzir `specs/NNN-feature-name/tasks.md` com lista ordenada de tasks.

## Estrutura obrigatória do tasks.md

```markdown
# Tasks: <Nome>

**Plan:** [plan.md](./plan.md)
**Total tasks:** N

## Execution Order

### Phase 1 — Foundation (parallel-safe)
- [T1] **<título>**
  - **Files:** `<paths>`
  - **Acceptance:** <critério verificável>
  - **Depends on:** none
  - **Estimated effort:** P/M/G

### Phase 2 — <nome>
- [T2] ...

## Parallelization
| Phase | Parallel tasks | Sequential after |
|-------|----------------|------------------|
| 1 | T1, T3, T4 | T2 |
| 2 | T5, T6 | T7 (depends on T5) |

## Risk Tasks
- [Tn] <task>: <risco específico>
```

## Regras

1. Cada task DEVE caber em ≤ 1 dia de trabalho
2. Cada task DEVE ter critério de aceite verificável (`mvn test`, endpoint responde, etc.)
3. Identifique paralelismo explicitamente (tasks em arquivos diferentes podem ir em paralelo)
4. Tasks de teste vão JUNTAS com a task que implementam (TDD-friendly)
5. Última task SEMPRE deve incluir: rodar suite completa de testes + build verde
6. Após criar, sugira `/implement` (ou execução manual)
