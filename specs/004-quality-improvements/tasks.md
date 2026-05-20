# Tasks: Quality Improvements

**Plan:** [plan.md](./plan.md)
**Total tasks:** 4

## Execution Order

### Phase 1 — Parallel

- [x] **T15** — `BusinessException` abstract sealed
  - **Agent:** @sre
  - **Files:** `BusinessException.java`, `ResourceNotFoundException.java`, `EmailAlreadyExistsException.java`, `RestExceptionHandler.java`, `RestExceptionHandlerTest.java`
  - **Acceptance:** Compila exhaustivo sem `default`; testes verdes
  - **Effort:** P

- [x] **T17** — Extrair lógica User para service
  - **Agent:** @dev-java
  - **Files:** `UserService.java`, `UserRestController.java`, `UserServiceTest.java`
  - **Acceptance:** Controller sem `RoleRepository`/`PasswordEncoder`; novo teste de `roleNotFound`
  - **Effort:** P

- [x] **T8** — Testes unitários para validators
  - **Agent:** @qa
  - **Files:** `SafeHtmlValidatorTest.java` (new), `StrongPasswordValidatorTest.java` (new)
  - **Acceptance:** Cobertura ≥ 90% em ambos; 59 testes novos
  - **Effort:** P

### Phase 2 — After sync

- [x] **T16** — Teste login com `accountLocked=true`
  - **Agent:** @qa
  - **Files:** `LoginTest.java`
  - **Acceptance:** Teste cria user com `accountLocked=true` via repository, login retorna 401
  - **Depends on:** T17 (UserService.save assinatura mudou)
  - **Effort:** P

## Parallelization

| Phase | Parallel | Sequential after |
|-------|----------|------------------|
| 1 | T15, T17, T8 | sync |
| 2 | T16 | (final) |

## Risk Tasks

- **T16:** Adicionou `UserRepository` e `PasswordEncoder` autowired ao `LoginTest`,
  mudando a Spring context cache key. Isso fez o `static headers` de
  `UserRestControllerTest` ficar com token de porta diferente. **Resolvido com:**
  - Port-aware header cache no `UserRestControllerTest`
  - `ApplicationContext.getBean()` no `LoginTest` (não muda cache key)
  - Rate limiter configurável (`login.rate-limit.max-attempts=100` em test)
