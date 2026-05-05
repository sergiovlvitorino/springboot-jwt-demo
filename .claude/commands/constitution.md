---
description: Cria ou atualiza memory/constitution.md com os princípios invioláveis do projeto.
---

# /constitution

Você está criando ou atualizando a **constituição** do projeto.

## Contexto

Leia primeiro `memory/constitution.md` (se existir) e o estado atual do projeto.

## Sua tarefa

A constituição define **princípios invioláveis** que todas as specs, plans, tasks
e implementações devem respeitar.

## Estrutura

```markdown
# Project Constitution — <Nome>

**Versão:** X.Y.Z
**Ratificado em:** <branch ou tag>

## Article I — <Tema>
1. <Princípio>
2. <Princípio>

## Article II — ...

## Governança
<Como modificar esta constituição>
```

## Regras

1. Princípios DEVEM ser específicos e verificáveis (não "código limpo", e sim
   "todos os DTOs DEVEM ser record")
2. Se atualizar uma constitution existente, bumpe a versão (semver)
3. Mudanças BREAKING exigem migração de specs/plans existentes
4. Cite o motivo histórico de cada artigo se útil (ex: "Travis proibido — substituído por GitHub Actions em 2026-04")
5. Comprometa em commit dedicado: `chore(constitution): bump to X.Y.Z`
