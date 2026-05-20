# Tasks: [FEATURE NAME]

**Plan:** [plan.md](./plan.md)
**Total tasks:** [N]

<!--
  Decomposição do plan em tasks executáveis (≤ 1 dia cada).
  Cada task tem ID Tn, agente responsável, arquivos tocados, critério de
  aceite verificável, dependências e estimativa de esforço (P/M/G).

  Convenções:
  - IDs de task podem reusar numeração existente do projeto (ex.: T6, T13)
    se a feature dá continuidade a um backlog. Caso contrário, T1, T2, ...
  - Agentes comuns: @dev-java, @qa, @sre, ou combinação (ex.: @dev-java + @qa).
  - Esforço: P (≤ ½ dia), M (≤ 1 dia), G (> 1 dia — considere quebrar).
  - Tasks já implementadas marcam o checkbox: [x].
-->

## Execution Order

### Phase 1 — [Foundation / Parallel / Independent]

<!--
  Tasks que podem rodar em paralelo (arquivos disjuntos, sem dependência).
  Use "Foundation" quando a fase é bloqueante para as demais.
-->

- [ ] **T[n]** — [título curto e ativo]
  - **Agent:** [@dev-java | @qa | @sre]
  - **Files:** `[caminho/Arquivo.java]`, `[caminho/outro.sql]` (new)
  - **Acceptance:** [critério verificável: `mvn test` verde, endpoint retorna X, etc.]
  - **Effort:** [P|M|G]

- [ ] **T[n]** — [título]
  - **Agent:** [@...]
  - **Files:** `[...]`
  - **Acceptance:** [...]
  - **Effort:** [P|M|G]

### Phase 2 — [After sync / Sequential]

<!--
  Tasks que dependem da Phase 1 (mesmos arquivos, ou precisam que algo
  da Phase 1 esteja mergeado). Declare a dependência explicitamente em
  "Depends on:".

  Para syncs críticos (ex.: refresh token espera UserDetailsAdapter),
  destaque com um bloco "### CRITICAL SYNC" entre as fases listando
  o que precisa estar mergeado e testado.
-->

- [ ] **T[n]** — [título]
  - **Agent:** [@...]
  - **Files:** `[...]`
  - **Acceptance:** [...]
  - **Depends on:** T[n] ([motivo: mesmo arquivo / API mudou / etc.])
  - **Effort:** [P|M|G]

<!--
  Phase 3 (opcional) — Post code review (ad-hoc)
  Use quando o code review de @tl-java identificar CRITICAL/MAJOR findings
  que precisam ser corrigidos antes do merge final. Numere como C1, C2, M1...

  - [ ] **C1** — [correção crítica]
  - [ ] **M1** — [correção major]
-->

## Parallelization

<!--
  Visão consolidada de quem pode rodar em paralelo dentro de cada fase.
-->

| Phase | Parallel | Sequential after |
|-------|----------|------------------|
| 1 | T[n], T[n], T[n] | sync |
| 2 | T[n] | (final) |

## Risk Tasks

<!--
  Tasks com risco específico (conflito de merge, bug latente descoberto,
  efeito em context cache, etc.). Descreva o risco e como mitigá-lo.
  Última task da feature SEMPRE deve incluir suite completa verde.
-->

- **T[n]:** [risco específico e mitigação]

<!--
  Follow-up (opcional)
  Findings do code review que NÃO foram corrigidos nesta feature e
  ficam para próxima sprint. Liste com IDs M[n] e descrição curta.

  ## Follow-up (não implementado)

  - **M[n]** — [pendência para próxima sprint]
-->
