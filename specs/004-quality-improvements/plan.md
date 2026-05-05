# Implementation Plan: Quality Improvements

**Spec:** [spec.md](./spec.md)
**Status:** Implemented

## Architecture

Quatro mudanças independentes, mas T16 (teste accountLocked) deve rodar após
T17 (mudança de assinatura do `UserService.save`).

```
Phase 1 (paralelo):
  ├── @sre        → BusinessException abstract sealed (T15)
  ├── @dev-java   → Extract User logic to service (T17)
  └── @qa         → Validator unit tests (T8)

Phase 2 (após sync):
  └── @qa         → accountLocked test (T16)
```

## Components

| Componente | Mudança |
|------------|---------|
| `domain/exception/BusinessException.java` | `abstract sealed`, construtor `protected` |
| `domain/exception/ResourceNotFoundException.java` | `final` |
| `domain/exception/EmailAlreadyExistsException.java` | `final` |
| `ui/rest/controller/RestExceptionHandler.java` | Handler dedicado por subclasse |
| `application/service/UserService.java` | Receber `SaveCommand`, lookup Role |
| `ui/rest/controller/UserRestController.java` | Remover lookup, injeções extras |
| `application/service/UserServiceTest.java` | Ajustar mocks para nova assinatura |
| `infrastructure/validations/SafeHtmlValidatorTest.java` | NEW — 19 testes |
| `infrastructure/validations/StrongPasswordValidatorTest.java` | NEW — 40 testes |
| `ui/rest/controller/test/LoginTest.java` | NEW — teste accountLocked |

## Data Model

Sem alteração.

## API Contracts

`POST /rest/user` — comportamento idêntico, mudança apenas interna.

## Dependencies

Nenhuma nova.

## Migrations

Nenhuma.

## Security Considerations

- Cobertura nos validators reduz risco de regressão em controles de XSS e
  fraqueza de senha
- Teste accountLocked garante que mecanismo de bloqueio funciona

## Constitutional Compliance Checklist

- [x] Article I: N/A
- [x] Article II: respeitado (controller fino)
- [x] Article III: `SaveCommand` é record
- [x] Article IV: abstract sealed, subclasses final, sem default
- [x] Article V: validators testados
- [x] Article VI: cobertura ≥ 90% nos validators
- [x] Article VII: N/A
- [x] Article VIII: API inalterada
- [x] Article IX: N/A
- [x] Article X: N/A
- [x] Article XI: N/A
- [x] Article XII: N/A

## Risks & Mitigations

| Risco | Mitigação |
|-------|-----------|
| `UserServiceTest` quebra com nova assinatura | Refatorar testes na mesma task |
| `RestExceptionHandler` conflita com handler de `AccessDeniedException` (Sprint 2) | Resolver no merge |
| Static `headers` em `UserRestControllerTest` falha após adição de novos beans | Port-aware caching + `ApplicationContext.getBean()` |
| Rate limiter bloqueia testes (10/min default) | Configurar `login.rate-limit.max-attempts=100` em test profile |
