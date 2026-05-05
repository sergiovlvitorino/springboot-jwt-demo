# Implementation Plan: Security Hardening

**Spec:** [spec.md](./spec.md)
**Status:** Implemented

## Architecture

Quatro mudanças com **alto risco de conflito** em `WebSecurityConfig.java`
(T5 e T14 ambos tocam). Sequenciamento obrigatório.

```
Phase 1 (paralelo):
  ├── @sre        → OWASP plugin + CI step (T18)
  ├── @dev-java   → Granular authorities (T5)
  └── @qa         → X-Forwarded-For (T7)

Phase 2 (após sync — depende de T5):
  └── @dev-java   → CSP + HSTS headers (T14)
```

## Components

| Componente | Tipo | Mudança |
|------------|------|---------|
| `domain/model/AuthorityConstants.java` | Constants | Adicionar `USER_DELETE` |
| `infrastructure/Initialize.java` | Seed | Persistir nova authority |
| `infrastructure/Config.java` | Config | Adicionar `@EnableMethodSecurity` |
| `ui/rest/controller/UserRestController.java` | Controller | Trocar `USER_SAVE` por `USER_DELETE` no delete |
| `ui/rest/controller/RestExceptionHandler.java` | Handler | Novo handler para `AccessDeniedException` → 403 |
| `infrastructure/security/LoginRateLimitFilter.java` | Filter | Método `extractClientIp()` lendo XFF |
| `infrastructure/security/WebSecurityConfig.java` | Config | Adicionar HSTS + CSP no headers builder |
| `pom.xml` | Build | Plugin `dependency-check-maven` |
| `.github/workflows/maven.yml` | CI | Job separado `dependency-check` |
| `dependency-check-suppressions.xml` | Config | Falsos positivos documentados |

## Data Model

Nenhuma alteração de schema (authorities são strings em memória + linhas em `authority`).

Seed adicional:
```java
authorityRepository.saveAll(List.of(
    new Authority("USER_RETRIEVE"),
    new Authority("USER_SAVE"),
    new Authority("USER_DELETE"),  // NEW
    new Authority("ROLE_RETRIEVE")
));
```

## API Contracts

`DELETE /rest/user/{id}` — autorização:
```yaml
required_authority: USER_DELETE  # was: USER_SAVE
on_missing_authority: 403 Forbidden
```

Headers em todas as respostas:
```
Strict-Transport-Security: max-age=31536000; includeSubDomains
Content-Security-Policy: default-src 'none'
```

## Dependencies

Nova dependência (apenas plugin Maven, não runtime):
```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>${dependency-check-maven.version}</version>
</plugin>
```

## Migrations

Nenhuma migration de banco. Seed atualizado em `Initialize.java`.

## Security Considerations

- **CRÍTICO:** Sem `@EnableMethodSecurity`, todos os `@PreAuthorize` são ignorados.
  Bug latente descoberto durante T5.
- **HSTS em testes HTTP:** Spring Security só envia HSTS em HTTPS por padrão.
  Para testes funcionarem, usar `AnyRequestMatcher.INSTANCE`.
- **XFF spoofing:** primeiro IP da cadeia confiado. Em produção, configurar
  trusted proxies via `server.forward-headers-strategy`.

## Constitutional Compliance Checklist

- [x] Article I (Stack): N/A
- [x] Article II (Layers): respeitado
- [x] Article III (Records): N/A
- [x] Article IV (Exceptions): handler 403 segue padrão
- [x] Article V (Security): authorities granulares, rate limit, HSTS, CSP
- [x] Article VI (Tests): novos testes para forbidden, XFF, headers
- [x] Article VII (Persistence): seed em profile dev/test apenas
- [x] Article VIII (REST): handler de erro padronizado
- [x] Article IX (Config): N/A
- [x] Article X (CI/CD): OWASP scan
- [x] Article XI (Container): N/A
- [x] Article XII (Docs): README atualizado

## Risks & Mitigations

| Risco | Mitigação |
|-------|-----------|
| `WebSecurityConfig` editado por T5 e T14 | Sequenciar: T5 primeiro, T14 após sync |
| HSTS não aparece em testes HTTP | `AnyRequestMatcher.INSTANCE` no `httpStrictTransportSecurity` |
| CI quebra sem `NVD_API_KEY` | `continue-on-error: true` no job OWASP |
| Bug latente: `@EnableMethodSecurity` ausente | Descoberto e corrigido em T5; testes 403 validam |
