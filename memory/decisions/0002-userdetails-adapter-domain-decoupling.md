# ADR-0002: UserDetailsAdapter — Domain Decoupling from Spring Security

**Status:** Accepted
**Date:** 2026-04-14

## Context

A integração padrão entre uma aplicação Spring Boot e o Spring Security costuma forçar a
entidade de usuário a implementar `org.springframework.security.core.userdetails.UserDetails`.
Essa abordagem, embora curta, traz acoplamentos indesejáveis:

- A entidade de domínio (`User`) passa a depender diretamente de tipos do Spring Security
  (`GrantedAuthority`, `UserDetails`).
- Mudanças no contrato `UserDetails` (novos métodos default, descontinuações) afetam a
  modelagem de domínio.
- O Article II da constituição é explícito: **`domain/` não depende de framework de
  apresentação ou segurança.**

A entidade `User` é JPA (`@Entity`), o que já é uma exceção tolerada para suportar o ORM.
Acumular um segundo acoplamento (Spring Security) na mesma classe rompe ainda mais o
isolamento do domínio.

## Decision

Manter `User` em `domain/model/` apenas com responsabilidades de domínio (campos,
relacionamentos JPA, getters/setters, `equals`/`hashCode`/`toString` mascarando senha) e
introduzir um adapter dedicado em `infrastructure/security/`:

```java
public class UserDetailsAdapter implements UserDetails {
    private final User user;
    public UserDetailsAdapter(User user) { this.user = user; }
    public User getUser() { return user; }
    // mapeia getAuthorities/getPassword/getUsername/isEnabled/...
}
```

O adapter é construído pelo `UserDetailsService` (camada `infrastructure/security/`), que
busca o `User` via `UserRepository` e o embrulha. Authorities são derivadas de
`user.getRole().getAuthorities()` e mapeadas para `SimpleGrantedAuthority`. Status
operacional (`enabled`, `accountLocked`) é lido do domínio.

Quando a stack downstream precisa de volta o `User` (por exemplo, em um service que
recebe o `Authentication` no controller), o adapter expõe `getUser()` — recuperando o
agregado de domínio sem reconsultas.

## Consequences

### Positivas
- O pacote `domain/` permanece livre de imports de Spring Security. Substituir Spring
  Security por outra solução (ex.: implementação custom de filter chain) não exige
  reescrever a entidade `User`.
- Responsabilidades separadas: regras de autenticação (locked, expired, credentials)
  ficam no adapter, próximas ao framework que as consome.
- Testes de domínio sobre `User` não precisam carregar contexto de segurança.

### Negativas / Trade-offs aceitos
- Uma camada de indireção a mais no fluxo de login: `UserDetailsService` → repositório →
  `User` → `UserDetailsAdapter` → Spring Security. O custo é desprezível (instanciação
  POJO por requisição autenticada).
- Duas representações do "usuário" no caminho de execução (`User` e `UserDetailsAdapter`).
  Mitigado mantendo o adapter sem estado próprio — apenas delega ao `User` interno.

### Neutras / Implicações operacionais
- Code review DEVE rejeitar imports de pacotes `org.springframework.security.*` dentro
  de `domain/`.
- Qualquer extensão futura de `UserDetails` (ex.: claims customizadas) é feita no adapter,
  sem impactar o domínio.
