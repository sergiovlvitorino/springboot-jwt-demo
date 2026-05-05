# Implementation Plan: Modernization Foundation

**Spec:** [spec.md](./spec.md)
**Status:** Implemented

## Architecture

Quatro mudanças independentes, agrupáveis em duas fases para minimizar conflitos
de merge:

```
Phase 1 (paralelo):
  ├── @sre        → CI/CD cleanup (T1)
  ├── @dev-java   → RoleResponse record (T2)
  └── @qa         → @MockitoBean migration (T12)

Phase 2 (após sync):
  └── @dev-java   → pageSize @Max(100) (T11)
```

## Components

| Componente | Tipo | Responsabilidade |
|------------|------|------------------|
| `.github/workflows/maven.yml` | CI | Build, test, JaCoCo, Codecov upload |
| `system.properties` | Config | Declarar Java 21 |
| `application/command/role/RoleResponse.java` | DTO | Schema estável de Role na API |
| `ui/rest/controller/RoleRestController.java` | Controller | Mapear `Role → RoleResponse` |
| `application/command/{user,role}/ListCommand.java` | Validation | `@Max(100)` em `pageSize` |
| `contract/BaseContractTest.java` | Tests | `@MockitoBean` no lugar de `@MockBean` |

## Data Model

Sem alteração no schema do banco. Apenas estrutura de DTOs:

```java
public record RoleResponse(
    UUID id,
    String name,
    List<String> authorities
) {
    public static RoleResponse from(Role role) { ... }
}
```

## API Contracts

`GET /rest/role` — schema atualizado:

```yaml
RoleResponse:
  type: object
  properties:
    id: { type: string, format: uuid }
    name: { type: string }
    authorities:
      type: array
      items: { type: string }
```

Validação adicional:
```yaml
ListCommand.pageSize:
  minimum: 1
  maximum: 100  # NEW
```

## Dependencies

Nenhuma dependência nova. Removido apenas o uso de `@MockBean` (já estava no
classpath via spring-boot-test).

## Migrations

Nenhuma migration de banco.

## Security Considerations

- `@Max(100)` no `pageSize` mitiga DoS via consumo de memória
- `RoleResponse` evita vazamento de coleções internas (`users`, etc.)
- CI com Codecov aumenta visibilidade sobre código não testado

## Constitutional Compliance Checklist

- [x] Article I (Stack): Java 21 declarado em `system.properties`
- [x] Article II (Layers): mudanças respeitam separação
- [x] Article III (Records): `RoleResponse` é record
- [x] Article IV (Exceptions): N/A
- [x] Article V (Security): paginação limitada
- [x] Article VI (Tests): `@MockitoBean`, suite verde
- [x] Article VII (Persistence): N/A
- [x] Article VIII (REST): DTO em vez de entidade, validação
- [x] Article IX (Config): N/A
- [x] Article X (CI/CD): GitHub Actions + Codecov + JaCoCo
- [x] Article XI (Container): N/A (Sprint 5)
- [x] Article XII (Docs): README atualizado

## Risks & Mitigations

| Risco | Mitigação |
|-------|-----------|
| Testes que usavam `pageSize=10000` quebram | Ajustar para `pageSize=100` ou adicionar teste 400 |
| Codecov upload falha sem `CODECOV_TOKEN` | `fail_ci_if_error: false` para não quebrar build |
| Cache key do Spring context muda com `@MockitoBean` | Build verde valida |
