---
description: Cria spec.md (O QUÊ e POR QUÊ) para uma nova feature, sem detalhes de implementação.
---

# /specify

Você está criando uma **specification** seguindo o método Spec-Driven Development.

## Contexto

Leia primeiro:
- `memory/constitution.md` — princípios invioláveis do projeto
- `specs/` — features já especificadas (para padrões e numeração)

## Sua tarefa

A partir do input do usuário (descrição em linguagem natural), produza o arquivo
`specs/NNN-feature-name/spec.md` onde:
- `NNN` é o próximo número sequencial (zero-padded a 3 dígitos)
- `feature-name` é kebab-case derivado do tema

## Estrutura obrigatória do spec.md

```markdown
# Feature: <Nome>

**Status:** Draft
**Author:** <usuário>
**Created:** <YYYY-MM-DD>
**Spec ID:** <NNN>

## Problem Statement
<Por que essa feature existe? Qual dor resolve?>

## User Stories
Como <persona>, quero <ação>, para <benefício>.

## Acceptance Criteria
- [AC-1] <critério verificável>
- [AC-2] ...

## Out of Scope
- <coisas que esta spec NÃO cobre>

## Open Questions
- [ ] <ambiguidades a resolver com /clarify>

## Constitutional Compliance
<Quais artigos da constitution se aplicam? Há conflito?>
```

## Regras

1. **PROIBIDO** detalhes técnicos (frameworks, classes, métodos, schemas)
2. Linguagem de **negócio**, não de implementação
3. Critérios de aceite DEVEM ser verificáveis (passa/falha)
4. Se a constitution for violada, marque com `⚠ CONSTITUTIONAL VIOLATION` e justifique
5. Se houver ambiguidade, registre em "Open Questions" — use `/clarify` depois
6. Após criar o arquivo, exiba o caminho e pergunte se o usuário quer prosseguir para `/plan`
