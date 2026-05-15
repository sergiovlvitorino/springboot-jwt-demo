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

- [x] Article I (Stack): N/A — sem alteração de versão de Java, Spring Boot, banco ou Maven
- [x] Article II (Layers): authorities permanecem em `domain/model/AuthorityConstants`, filtro em `infrastructure/security/`, handler em `ui/rest/controller/`; sem vazamento de Spring Security para o domínio
- [x] Article III (Records): N/A — feature não cria DTOs novos
- [x] Article IV (Exceptions): handler para `AccessDeniedException` adicionado a `RestExceptionHandler` retornando 403 com `ErrorBean`, sem expor classe/stack trace e sem branch default
- [x] Article V (Security): `USER_DELETE` separa privilégio (item 8); `LoginRateLimitFilter.extractClientIp()` lê `X-Forwarded-For` para rate limit correto atrás de proxy (item 5); HSTS `max-age=31536000; includeSubDomains` e CSP `default-src 'none'` adicionados (item 6); `@EnableMethodSecurity` ativa `@PreAuthorize` (bug latente corrigido)
- [x] Article VI (Tests): novos testes cobrem 403 sem `USER_DELETE` (AC-2), XFF com IP único e múltiplos (AC-4 a AC-6), e presença de headers HSTS/CSP (AC-7 a AC-9)
- [x] Article VII (Persistence): seed atualizado em `Initialize.java` (já restrito a `@Profile({"dev","test"})` por Spec 003); sem mudança de schema
- [x] Article VIII (REST): respostas 403 padronizadas em `ErrorBean`; sem mudança de DTO ou paginação
- [x] Article IX (Config): N/A — sem novas env vars ou profiles (`NVD_API_KEY` é apenas CI, não runtime)
- [x] Article X (CI/CD): job dedicado `dependency-check` no `maven.yml` configurado para falhar build em CVSS ≥ 7 com `continue-on-error: true` durante adoção (AC-10 a AC-12)
- [x] Article XI (Container): N/A — Dockerfile entregue em Spec 005
- [x] Article XII (Docs): README atualizado descrevendo `USER_DELETE`, headers de segurança e escopo do rate limit; `dependency-check-suppressions.xml` documenta falsos positivos

## Risks & Mitigations

| Risco | Mitigação |
|-------|-----------|
| `WebSecurityConfig` editado por T5 e T14 | Sequenciar: T5 primeiro, T14 após sync |
| HSTS não aparece em testes HTTP | `AnyRequestMatcher.INSTANCE` no `httpStrictTransportSecurity` |
| CI quebra sem `NVD_API_KEY` | `continue-on-error: true` no job OWASP |
| Bug latente: `@EnableMethodSecurity` ausente | Descoberto e corrigido em T5; testes 403 validam |
