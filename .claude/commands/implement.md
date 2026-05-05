---
description: Executa as tasks de uma feature spec'd e plan'd.
---

# /implement

Você vai executar a implementação de uma feature seguindo o plan + tasks.

## Contexto OBRIGATÓRIO

Leia ANTES de qualquer modificação:
- `memory/constitution.md`
- `specs/NNN-feature-name/spec.md`
- `specs/NNN-feature-name/plan.md`
- `specs/NNN-feature-name/tasks.md`
- `specs/NNN-feature-name/data-model.md` (se existir)
- `specs/NNN-feature-name/contracts/` (se existir)

## Sua tarefa

Executar as tasks na ordem definida em `tasks.md`. Para cada task:

1. **Anuncie** qual task está executando
2. **Implemente** seguindo estritamente o plan
3. **Valide** o critério de aceite (rode comando relevante)
4. **Commit** com mensagem descritiva referenciando a task

## Regras

1. **NUNCA** altere a spec ou o plan durante a implementação — se descobrir
   problema, **PARE** e peça atualização da spec/plan
2. Use sub-agentes em paralelo quando `tasks.md` indicar tasks paralelas
3. Após cada fase, rode `mvn test` e confirme verde antes de prosseguir
4. Code review com `@tl-java` ao final
5. Atualize a documentação (README, Postman) ao final
6. Marque tasks completadas em `tasks.md` (`- [x]`)

## Sub-agente sugerido por tipo de task

| Tipo | Agente |
|------|--------|
| Código de produção (services, controllers, entities) | `@dev-java` |
| Testes (unit, integration) | `@qa` |
| CI/CD, Docker, Flyway, infra | `@sre` |
| Code review | `@tl-java` |
