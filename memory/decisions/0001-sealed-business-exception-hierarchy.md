# ADR-0001: Sealed BusinessException Hierarchy

**Status:** Accepted
**Date:** 2026-04-14

## Context

Exceções de negócio são parte do contrato de domínio: cada uma representa um cenário
inválido nomeado (`ResourceNotFoundException`, `EmailAlreadyExistsException`,
`InvalidRefreshTokenException`). Antes da consolidação atual, o tratamento de erros
dependia de um único `@ExceptionHandler(Exception.class)` que mapeava tudo para 500 ou
inferia status por instância — abordagem com dois problemas concretos:

1. **Mascaramento silencioso**: novas exceções introduzidas em features posteriores caíam
   no handler genérico e retornavam mensagens inadequadas (ou stack trace) ao cliente.
2. **Falta de awareness na revisão**: nada no compilador forçava o autor a pensar no
   handler HTTP correspondente quando criava uma exceção nova.

Java 21 disponibiliza `sealed classes`, que permitem fechar a hierarquia em tempo de
compilação e habilitar exhaustiveness checking — encaixe direto para o domínio de
exceções, onde a lista de subtipos é deliberadamente finita.

## Decision

Modelaremos a hierarquia de exceções de negócio como **sealed**:

```java
public abstract sealed class BusinessException extends RuntimeException
        permits ResourceNotFoundException,
                EmailAlreadyExistsException,
                InvalidRefreshTokenException { ... }
```

Subclasses DEVEM ser declaradas `final` (já é o caso de todas as três). Cada subclasse
tem um `@ExceptionHandler` dedicado em
`ui/rest/controller/RestExceptionHandler.java`, mapeando para um status HTTP e um
`errorCode` específicos:

| Exceção | Status | errorCode |
|---------|--------|-----------|
| `ResourceNotFoundException` | 404 | `NOT_FOUND` |
| `EmailAlreadyExistsException` | 422 | `EMAIL_ALREADY_EXISTS` |
| `InvalidRefreshTokenException` | 401 | `INVALID_REFRESH_TOKEN` |

O handler genérico `@ExceptionHandler(Exception.class)` permanece **apenas** para falhas
inesperadas (500 + log de stack trace). NÃO é aceitável que uma `BusinessException` caia
nele — isso indica handler ausente.

Esta regra está codificada no Article IV da constituição.

## Consequences

### Positivas
- Adicionar nova exceção de negócio exige duas alterações simultâneas: cláusula
  `permits` em `BusinessException` E novo handler em `RestExceptionHandler`. O compilador
  rejeita a primeira sem a segunda quando a hierarquia é exercitada exaustivamente.
- Status HTTP e `errorCode` ficam explícitos por exceção — eliminado o mascaramento.
- Mensagens internas (nomes de classe, stack traces) não vazam para o cliente.

### Negativas / Trade-offs aceitos
- Hierarquia fechada **dificulta extensão por terceiros** (nenhum impacto: este é um
  monolito, sem plugins externos).
- Cada nova exceção exige três arquivos tocados (subclasse + `permits` + handler). É um
  custo de fricção deliberado, alinhado ao objetivo de awareness.

### Neutras / Implicações operacionais
- Code review DEVE rejeitar PRs que adicionem subclasse de `BusinessException` sem o
  handler correspondente.
- Testes de contrato (Spring Cloud Contract) cobrem as respostas de erro padronizadas em
  `ErrorBean`.
