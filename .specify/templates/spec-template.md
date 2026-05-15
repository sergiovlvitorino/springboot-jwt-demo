# Feature: [FEATURE NAME]

**Status:** Draft
**Author:** [autor]
**Created:** [YYYY-MM-DD]
**Spec ID:** [NNN]
**Sprint:** [N]

<!--
  Input do usuário (descrição livre): "$ARGUMENTS"

  Esta spec descreve O QUE e POR QUE da feature, sem detalhes de implementação.
  Linguagem de negócio, não de framework. Detalhes técnicos vão no plan.md.
-->

## Problem Statement

<!--
  1 a 3 parágrafos explicando a dor ou oportunidade.
  - Foque no problema, não na solução.
  - Evite mencionar classes, endpoints, libs ou schemas.
  - Se houver múltiplas frentes (ex.: UX + segurança + ops), liste-as.
-->

## User Stories

<!--
  Lista flat (sem priorização P1/P2/P3). Cada história no formato:
    - Como [persona], quero [ação], para [valor].
  Personas comuns no projeto: usuário autenticado, mantenedor, operador,
  administrador de segurança, auditor, DBA, arquiteto, dev/operador.
-->

- Como [persona], quero [ação], para [valor].
- Como [persona], quero [ação], para [valor].

## Acceptance Criteria

<!--
  Critérios verificáveis (passa/falha). Numere como [AC-N].
  Se a feature tem sub-áreas (ex.: T6, T9, T13 numa mesma spec),
  agrupe sob subseções "### Sub-área (Tn)" e numere [AC-N] continuamente.
-->

- [AC-1] [descrição objetiva e verificável]
- [AC-2] [descrição objetiva e verificável]
- [AC-3] [descrição objetiva e verificável]

## Out of Scope

<!--
  O que esta spec NÃO cobre. Tão importante quanto o escopo.
  Útil para barrar scope creep durante /plan e /tasks.
-->

- [item explicitamente fora]
- [item explicitamente fora]

## Open Questions

<!--
  Ambiguidades ou decisões pendentes. Resolver via /clarify antes do /plan.
  Ao resolver, marque com ~~tachado~~ → resposta. Ex.:
    - ~~Token UUID ou bytes aleatórios?~~ → UUID (suficiente para MVP)

  Se não há perguntas em aberto, deixe a seção vazia ou escreva "(nenhuma)".
-->

- [ ] [pergunta a esclarecer]

## Constitutional Compliance

<!--
  Mapeamento article-by-article de memory/constitution.md.
  Use ✓ para aderência, ⚠ para alerta/atenção, ✗ + justificativa para violação.
  Liste apenas os artigos relevantes à feature (não precisa listar todos os 12).
-->

- ✓ Article [N] ([Nome]): [como a feature respeita este artigo]
- ⚠ Article [N] ([Nome]): [alerta — ex.: exige adição de seed/migration]
