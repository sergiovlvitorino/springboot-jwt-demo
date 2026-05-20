# Feature: Quality Improvements

**Status:** Implemented
**Author:** sergio
**Created:** 2026-04-13
**Spec ID:** 004
**Sprint:** 4

## Problem Statement

Análise de qualidade identificou:
- `BusinessException` é sealed mas não abstract — pode ser instanciada diretamente,
  criando branch `default` inalcançável no switch do exception handler
- `UserRestController.post()` faz lookup de Role e constrói entidade — lógica de
  negócio vazada para a camada de apresentação
- Validators customizados (`SafeHtmlValidator`, `StrongPasswordValidator`) não têm
  testes unitários dedicados, dependendo apenas de testes de integração
- Não existe teste que valide que `accountLocked=true` impede login

## User Stories

- Como **mantenedor**, quero hierarquia de exceções estritamente exhaustiva,
  para que o compilador detecte casos não tratados.
- Como **arquiteto**, quero controllers finos (apenas delegação), para concentrar
  regras de negócio em uma camada.
- Como **revisor**, quero cobertura ≥ 90% nos validators de segurança, para ter
  confiança em mudanças futuras.
- Como **auditor**, quero teste explícito de bloqueio de conta, para garantir
  que o mecanismo `accountLocked` funciona.

## Acceptance Criteria

- [AC-1] `BusinessException` é `abstract sealed class`
- [AC-2] `ResourceNotFoundException` e `EmailAlreadyExistsException` são `final`
- [AC-3] `RestExceptionHandler` tem handler dedicado por subclasse (sem `default`)
- [AC-4] `UserService.save()` recebe `SaveCommand`, não `User`
- [AC-5] `UserService.save()` faz lookup de Role internamente
- [AC-6] `UserRestController.post()` apenas delega para o service
- [AC-7] `UserRestController` não injeta mais `RoleRepository` nem `PasswordEncoder`
- [AC-8] `SafeHtmlValidator` tem cobertura ≥ 90% (instruções e branches)
- [AC-9] `StrongPasswordValidator` tem cobertura ≥ 90%
- [AC-10] Existe teste que valida que login com `accountLocked=true` retorna 401
- [AC-11] Suite de testes verde

## Out of Scope

- Refatoração de outros controllers além de User
- Mudança nos validators (apenas criar testes)
- Mecanismo de bloqueio automático após N tentativas

## Open Questions

(Resolvidas durante implementação)

## Constitutional Compliance

- ✓ Article II (Layers): controller fino, regra no service
- ✓ Article IV (Exceptions): abstract sealed, subclasses final
- ✓ Article VI (Tests): cobertura, validators, accountLocked
