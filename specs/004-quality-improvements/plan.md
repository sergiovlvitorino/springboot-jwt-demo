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

- [x] Article I (Stack): N/A — sem mudança de versão, banco ou build tool
- [x] Article II (Layers): `UserRestController.post()` passa a apenas delegar para `UserService.save(SaveCommand)`; lookup de Role e construção da entidade migram para `application/service/`, controller deixa de injetar `RoleRepository` e `PasswordEncoder` (AC-6, AC-7)
- [x] Article III (Records): `SaveCommand` continua como `record`; `UserService.save()` agora consome o record diretamente (AC-4)
- [x] Article IV (Exceptions): `BusinessException` torna-se `abstract sealed class` com construtor `protected` (AC-1), `ResourceNotFoundException` e `EmailAlreadyExistsException` são `final` (AC-2), `RestExceptionHandler` ganha handler dedicado por subclasse sem branch default (AC-3) — alinhado a TODOS os 5 itens do artigo
- [x] Article V (Security): novos testes garantem que `accountLocked=true` retorna 401 (AC-10) e que validators de XSS (`SafeHtmlValidator`) e força de senha (`StrongPasswordValidator`) funcionam corretamente
- [x] Article VI (Tests): `SafeHtmlValidatorTest` (19 testes) e `StrongPasswordValidatorTest` (40 testes) atingem ≥ 90% de instruções e branches conforme item 2 do artigo (AC-8, AC-9); `LoginTest` cobre accountLocked (AC-10); suite verde (AC-11)
- [x] Article VII (Persistence): N/A — sem mudança de schema ou auditoria
- [x] Article VIII (REST): contrato de `POST /rest/user` permanece idêntico ao consumidor externo; `ErrorBean` mantém o formato de erro padronizado mesmo com handlers refatorados
- [x] Article IX (Config): N/A — apenas `login.rate-limit.max-attempts=100` em test profile (já existente para evitar bloqueio durante testes)
- [x] Article X (CI/CD): N/A — sem mudança de pipeline; cobertura adicional será refletida no relatório JaCoCo já configurado
- [x] Article XI (Container): N/A — containerização tratada em Spec 005
- [x] Article XII (Docs): N/A — feature é refator interno sem impacto em README, ADR ou Postman

## Risks & Mitigations

| Risco | Mitigação |
|-------|-----------|
| `UserServiceTest` quebra com nova assinatura | Refatorar testes na mesma task |
| `RestExceptionHandler` conflita com handler de `AccessDeniedException` (Sprint 2) | Resolver no merge |
| Static `headers` em `UserRestControllerTest` falha após adição de novos beans | Port-aware caching + `ApplicationContext.getBean()` |
| Rate limiter bloqueia testes (10/min default) | Configurar `login.rate-limit.max-attempts=100` em test profile |
