# Memory

Arquivos de contexto persistente do projeto, lidos por assistentes de IA antes
de qualquer trabalho substantivo.

## Conteúdo

- [constitution.md](./constitution.md) — princípios **invioláveis** do projeto.
  Toda spec, plan e implementação DEVE respeitar.

## Quando atualizar

| Arquivo | Quando |
|---------|--------|
| `constitution.md` | Decisão arquitetural fundamental que afeta todo o código futuro. Bumpe versão (semver). |

## Quando NÃO atualizar

- Decisões específicas de uma feature → vai em `specs/NNN/plan.md`
- Tutorial / exemplos → vai em `README.md` do projeto
- Histórico de implementação → `git log` é a fonte
