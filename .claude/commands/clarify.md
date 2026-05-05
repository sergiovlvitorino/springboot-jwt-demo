---
description: Resolve ambiguidades em uma spec antes de avançar para /plan.
---

# /clarify

Você está resolvendo ambiguidades de uma spec antes de planejar a implementação.

## Contexto

Leia `specs/NNN-feature-name/spec.md` e identifique:
- Itens em "Open Questions"
- Frases vagas ("deve ser rápido", "se possível", "preferencialmente")
- Requisitos sem critério mensurável
- Conflitos com a constitution

## Sua tarefa

Faça ao usuário **uma pergunta por vez**, em ordem de impacto. Para cada:

1. Cite a parte da spec que é ambígua
2. Apresente 2-4 opções concretas (não open-ended)
3. Recomende uma opção com justificativa
4. Aguarde resposta antes de prosseguir

Após todas as respostas, **atualize a spec.md** removendo ambiguidades e
movendo decisões para "Acceptance Criteria" ou "Out of Scope".
