---
description: Verifica consistência entre constitution, spec, plan e tasks de uma feature.
---

# /analyze

Você está validando a consistência de uma feature antes da implementação.

## Contexto

Leia:
- `memory/constitution.md`
- `specs/NNN-feature-name/spec.md`
- `specs/NNN-feature-name/plan.md`
- `specs/NNN-feature-name/tasks.md`
- Outros arquivos da feature

## Análise obrigatória

1. **Spec ↔ Plan:** todo Acceptance Criteria está coberto por algum componente do plan?
2. **Plan ↔ Tasks:** todo componente do plan tem task correspondente?
3. **Constitution ↔ Plan:** todos os 12 artigos foram avaliados?
4. **Tasks ↔ Tasks:** dependências circulares? Tasks que poderiam ser paralelizadas mas estão sequenciais?
5. **Riscos não mitigados:** algum item em "Risks" sem mitigação concreta?
6. **Critérios não verificáveis:** algum AC ou critério de task que não pode ser testado automaticamente?

## Output

Tabela de achados classificados:

| Severidade | Item | Local | Recomendação |
|------------|------|-------|--------------|
| CRITICAL | ... | spec.md AC-3 | ... |
| MAJOR | ... | plan.md §Architecture | ... |
| MINOR | ... | tasks.md T5 | ... |

Veredito final: `READY FOR /implement` ou `BLOCKED — fix CRITICALs first`.
