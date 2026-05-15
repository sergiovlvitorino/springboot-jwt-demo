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

- [x] Article I (Stack): `system.properties` declara `java.runtime.version=21`, alinhado ao Java 21 exigido pelo projeto (AC-3)
- [x] Article II (Layers): `RoleResponse` reside em `application/command/role/`, controller em `ui/rest/controller/` apenas mapeia entidade → DTO, sem violar fronteiras
- [x] Article III (Records): `RoleResponse` é declarado como `record` com factory `from(Role)`, alinhado ao mandato de DTOs imutáveis (AC-4, AC-5)
- [x] Article IV (Exceptions): N/A — feature não introduz nem altera hierarquia de exceções de negócio
- [x] Article V (Security): `@Max(100)` em `pageSize` mitiga DoS por consumo de memória em listagens públicas (AC-6); demais itens (BCrypt, JWT, headers, PII) inalterados
- [x] Article VI (Tests): suite verde após mudanças (AC-8); `@MockitoBean` substitui `@MockBean` deprecated em `BaseContractTest` (AC-7)
- [x] Article VII (Persistence): N/A — sem mudanças de schema, migrations ou auditoria
- [x] Article VIII (REST): `GET /rest/role` retorna `RoleResponse` em vez de entidade `Role` (AC-4); `ListCommand.pageSize` valida `@Max(100)` retornando 400 quando excedido (AC-6)
- [x] Article IX (Config): N/A — sem novas properties ou profiles
- [x] Article X (CI/CD): `.github/workflows/maven.yml` substitui Travis (AC-1) e executa build, test, JaCoCo report e Codecov upload (AC-2)
- [x] Article XI (Container): N/A — containerização entregue em Sprint 5 (Spec 005)
- [x] Article XII (Docs): README atualizado refletindo o badge de cobertura e o schema `RoleResponse`

## Risks & Mitigations

| Risco | Mitigação |
|-------|-----------|
| Testes que usavam `pageSize=10000` quebram | Ajustar para `pageSize=100` ou adicionar teste 400 |
| Codecov upload falha sem `CODECOV_TOKEN` | `fail_ci_if_error: false` para não quebrar build |
| Cache key do Spring context muda com `@MockitoBean` | Build verde valida |
