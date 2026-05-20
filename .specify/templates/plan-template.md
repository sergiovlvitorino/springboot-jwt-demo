# Implementation Plan: [FEATURE NAME]

**Spec:** [spec.md](./spec.md)
**Status:** Draft

<!--
  Este plan descreve COMO a feature será implementada (decisões técnicas).
  O QUE/POR QUÊ está em spec.md. Tasks executáveis vão em tasks.md.

  Detalhes longos (entidades, schemas, OpenAPI, trade-offs) vão para arquivos
  auxiliares opcionais nesta mesma pasta:
    - data-model.md   (entidades, índices, FKs)
    - contracts/      (OpenAPI YAML, JSON Schema)
    - research.md     (alternativas analisadas, decisões com trade-off)
    - quickstart.md   (passo a passo para validar manualmente)
-->

## Architecture

<!--
  Decisões arquiteturais de alto nível.
  - Sequenciamento de tasks (foundational vs paralelas) — diagrama ASCII ajuda.
  - Quais camadas serão tocadas (domain, application, infrastructure, ui).
  - Pontos de sync obrigatório (quando duas tasks tocam o mesmo arquivo).
-->

```
Phase 1 (paralelo):
  ├── @sre        → [task descritiva] (Tn)
  ├── @dev-java   → [task descritiva] (Tn)
  └── @qa         → [task descritiva] (Tn)

Phase 2 (após sync):
  └── @dev-java   → [task descritiva] (Tn)
```

## Components

<!--
  Tabela de componentes novos/modificados, com responsabilidade ou tipo
  de mudança. Se a feature tem várias frentes (ex.: T6, T9, T13), use
  subseções "### Tn — Nome".
-->

| Componente | Tipo | Responsabilidade / Mudança |
|------------|------|----------------------------|
| `[caminho/Arquivo.java]` | [Controller/Service/Entity/Filter/Config/...] | [o que muda] |
| `[caminho/Arquivo.java]` | [...] | [...] |

## Data Model

<!--
  Resumo do schema (tabelas, colunas, FKs, índices) ou "Sem alteração".
  Se o detalhe for extenso, mover para data-model.md e referenciar:
    Ver [data-model.md](./data-model.md).
-->

[Sem alteração] **OU** [resumo curto do schema] **OU** [Ver data-model.md]

## API Contracts

<!--
  Resumo dos endpoints novos/alterados e schemas de request/response.
  Trechos YAML são bem-vindos. Para contratos extensos, mover para
  contracts/ e referenciar: Ver [contracts/auth.yaml](./contracts/auth.yaml).
-->

[Sem mudança de contrato externo] **OU** [resumo dos endpoints] **OU** [Ver contracts/]

## Dependencies

<!--
  Bibliotecas novas no pom.xml com justificativa. Se nenhuma, declarar
  "Nenhuma dependência nova".
-->

[Nenhuma nova] **OU** [bloco XML do pom + justificativa]

## Migrations

<!--
  Lista de migrations Flyway necessárias (h2 + postgresql) ou "Nenhuma".
  Lembre: Article VII exige migrations vendor-specific separadas.
-->

[Nenhuma] **OU** [lista: V[N]__[descricao].sql (h2 + postgresql)]

## Security Considerations

<!--
  Authorities, validações, rate limit, sanitização, encoding de senha,
  PII em logs, fail-fast em vars obrigatórias. Sinalize bugs latentes
  ou armadilhas (ex.: HSTS só em HTTPS, XFF spoofing) com **CRÍTICO:**.
-->

- [consideração 1]
- [consideração 2]

## Constitutional Compliance Checklist

<!--
  Marque [x] em todos os artigos relevantes. N/A é aceitável quando
  o artigo realmente não se aplica. Se algum item NÃO for marcável,
  pare e justifique antes de continuar.
-->

- [ ] Article I (Stack): [como respeita ou N/A]
- [ ] Article II (Layers): [como respeita ou N/A]
- [ ] Article III (Records): [DTOs como record ou N/A]
- [ ] Article IV (Exceptions): [sealed + final + permits ou N/A]
- [ ] Article V (Security): [authorities, JWT, rate limit, headers, PII ou N/A]
- [ ] Article VI (Tests): [unit + integration planejados ou N/A]
- [ ] Article VII (Persistence): [Flyway, validate, audit, vendor ou N/A]
- [ ] Article VIII (REST): [DTOs, validation, paginação, OpenAPI ou N/A]
- [ ] Article IX (Config): [env vars, profiles ou N/A]
- [ ] Article X (CI/CD): [pipeline cobre ou N/A]
- [ ] Article XI (Container): [multi-stage, non-root, fail-fast ou N/A]
- [ ] Article XII (Docs): [README/Postman atualizados ou N/A]

## Risks & Mitigations

<!--
  Riscos identificados e como mitigá-los. Útil incluir riscos de merge
  (arquivo tocado por mais de uma task), dependências entre tasks e
  efeitos colaterais (ex.: Spring context cache key).
-->

| Risco | Mitigação |
|-------|-----------|
| [risco específico] | [como será mitigado] |
| [risco específico] | [como será mitigado] |
