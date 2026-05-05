# Feature: Modernization Foundation

**Status:** Implemented
**Author:** sergio
**Created:** 2026-04-13
**Spec ID:** 001
**Sprint:** 1

## Problem Statement

O projeto carregava débitos técnicos de versões anteriores: pipeline CI obsoleto
(Travis), exposição de entidades JPA na API REST, ausência de limites em paginação
(vetor para DoS) e uso de annotations deprecated do Spring Boot. Esses débitos
impediam manutenção saudável e tinham impactos diretos em segurança, performance
e onboarding.

## User Stories

- Como **mantenedor**, quero pipeline CI moderno e cobertura visível no badge,
  para confiar no estado do build a cada commit.
- Como **consumidor da API**, quero respostas com schema estável e desacoplado da
  persistência, para que mudanças internas não quebrem meu cliente.
- Como **operador**, quero proteção contra requests abusivos de paginação, para
  evitar OOM e degradação de serviço.
- Como **desenvolvedor**, quero usar APIs de teste atuais (não-deprecated), para
  manter compatibilidade com Spring Boot 3.5+.

## Acceptance Criteria

- [AC-1] `.travis.yml` removido do repositório
- [AC-2] GitHub Actions executa: build, test, JaCoCo report (artefato), Codecov upload
- [AC-3] `system.properties` declara Java 21 (não 18)
- [AC-4] Endpoint `GET /rest/role` retorna `RoleResponse` (record), não entidade `Role`
- [AC-5] `RoleResponse` contém apenas: `id`, `name`, `authorities` (List<String>)
- [AC-6] Endpoints de listagem (`/rest/user`, `/rest/role`) retornam HTTP 400
        quando `pageSize > 100`
- [AC-7] Nenhum teste do projeto usa `@MockBean` (substituído por `@MockitoBean`)
- [AC-8] Suite de testes verde após todas as mudanças

## Out of Scope

- Refatoração de outros endpoints além de Role (User já tinha DTO)
- Mudança no comportamento de paginação além do limite máximo
- Substituição de outras annotations deprecated não relacionadas a `@MockBean`

## Open Questions

(Resolvidas durante implementação)

## Constitutional Compliance

- ✓ Article I (Stack): Java 21 declarado
- ✓ Article III (Records): `RoleResponse` é record
- ✓ Article VI (Tests): `@MockitoBean`, suite verde
- ✓ Article VIII (REST): DTO em vez de entidade, paginação com `@Max(100)`
- ✓ Article X (CI/CD): GitHub Actions com JaCoCo + Codecov
