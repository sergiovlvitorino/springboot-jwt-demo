# Architecture Decision Records (ADRs)

Este diretório registra **decisões arquiteturais significativas** do projeto, no formato
Michael Nygard simplificado.

## O que é um ADR?

Um ADR documenta uma decisão arquitetural que:

- Tem **impacto duradouro** (afeta mais de uma feature ou módulo)
- Envolve **trade-offs explícitos** (alternativas foram consideradas e rejeitadas)
- É **difícil ou cara de reverter** (esforço significativo se mudada depois)
- Cria **regra ou convenção** que outras pessoas/agentes precisarão respeitar

ADRs NÃO são para escolhas triviais (nome de variável, formato de log) nem para decisões
totalmente ditadas pela constituição (essas vivem em `memory/constitution.md`).

## Quando criar um ADR

Crie um novo ADR quando:

- Adotar um novo pattern arquitetural (CQRS, Event Sourcing, Saga, etc.)
- Introduzir nova biblioteca/framework com impacto transversal
- Mudar a forma como uma camada se comunica com outra
- Definir convenção de modelagem (sealed hierarchies, adapter patterns, etc.)
- Tomar decisão de segurança/deploy/persistência com consequência operacional
- **Reverter ou substituir** decisão prévia (status `Superseded by ADR-XXXX`)

Quando em dúvida, escreva. ADR enxuto (40-80 linhas) é melhor que decisão tácita.

## Formato

Use `_template.md` como ponto de partida. Estrutura mínima:

1. **Title** — `ADR-NNNN: Título imperativo`
2. **Status** — `Proposed` | `Accepted` | `Deprecated` | `Superseded by ADR-XXXX`
3. **Date** — `YYYY-MM-DD` (data da decisão, não do registro)
4. **Context** — forças, restrições, problema
5. **Decision** — o que foi decidido, em voz ativa
6. **Consequences** — positivas, negativas/trade-offs e neutras

## Numeração e nome de arquivo

- Padrão: `NNNN-kebab-case.md` (zero-padded a **4 dígitos**)
- Exemplos: `0001-sealed-business-exception.md`, `0017-adopt-redis-cache.md`
- Numere sequencialmente, sem buracos
- ADRs **nunca** são deletados; quando obsoletos, mude o status para `Deprecated` ou
  `Superseded by ADR-XXXX` e mantenha o arquivo

## Índice

| ID | Título | Status |
|----|--------|--------|
| [0001](./0001-sealed-business-exception-hierarchy.md) | Sealed BusinessException Hierarchy | Accepted |
| [0002](./0002-userdetails-adapter-domain-decoupling.md) | UserDetailsAdapter — Domain Decoupling from Spring Security | Accepted |
| [0003](./0003-refresh-token-one-time-use-rotation.md) | Refresh Token with One-Time-Use Rotation | Accepted |
| [0004](./0004-multistage-dockerfile-non-root-runtime.md) | Multi-Stage Dockerfile with Non-Root Runtime | Accepted |
