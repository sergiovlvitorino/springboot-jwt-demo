# Feature: Refresh Token & Containerization

**Status:** Implemented
**Author:** sergio
**Created:** 2026-04-14
**Spec ID:** 005
**Sprint:** 5

## Problem Statement

Três lacunas funcionais e operacionais:

1. **UX de autenticação:** access token expira em 1h em prod. Usuário precisa
   re-digitar credenciais — fricção e incentivo a tokens com expiração longa
   (inseguro).
2. **Acoplamento de domínio:** entidade `User` implementa `UserDetails` do
   Spring Security, violando separação de concerns. Mudanças em Spring Security
   forçam mudanças no domínio.
3. **Setup operacional:** projeto sem Dockerfile nem compose. Onboarding e deploy
   manuais. Sem paridade dev/prod.

## User Stories

- Como **usuário autenticado**, quero renovar minha sessão sem re-digitar
  credenciais, para não ser interrompido durante uso.
- Como **arquiteto**, quero domínio independente de framework de segurança, para
  poder substituir Spring Security futuramente sem rewrite.
- Como **dev/operador**, quero rodar a aplicação completa com `docker compose up`,
  para onboarding em minutos e paridade dev/prod.

## Acceptance Criteria

### Refresh Token (T6)
- [AC-1] Login retorna access token (header) **e** refresh token (body JSON)
- [AC-2] `POST /auth/refresh` com refresh token válido retorna novo par de tokens
- [AC-3] Refresh token usado UMA vez não pode ser reutilizado (one-time, retorna 401)
- [AC-4] Refresh token expirado retorna 401
- [AC-5] Refresh token de usuário com `accountLocked=true` retorna 401
- [AC-6] Refresh token de usuário com `enabled=false` retorna 401
- [AC-7] Refresh token tem expiração configurável (`jwt.refresh-expiration`, default 7 dias)
- [AC-8] Access token tem claim `token_type: "access"`
- [AC-9] Endpoint `/auth/refresh` é protegido pelo rate limiter
- [AC-10] `RefreshRequest` valida `@NotBlank` em `refreshToken`

### UserDetails Adapter (T9)
- [AC-11] `User.java` NÃO implementa `UserDetails`
- [AC-12] `User.java` NÃO importa nada de Spring Security
- [AC-13] `UserDetailsAdapter` em `infrastructure/security/` implementa `UserDetails`
- [AC-14] `UserService.loadUserByUsername()` retorna `UserDetailsAdapter`
- [AC-15] Adapter expõe o `User` original via `getUser()`
- [AC-16] Login continua funcionando idêntico ao usuário externo

### Containerization (T13)
- [AC-17] `Dockerfile` multi-stage (build com Maven JDK 21, runtime com JRE 21)
- [AC-18] Imagem runtime roda com **non-root user**
- [AC-19] `docker-compose.yml` sobe app + PostgreSQL com health check
- [AC-20] `docker-compose.dev.yml` sobe apenas PostgreSQL para dev local
- [AC-21] `DB_PASSWORD` e `JWT_SECRET` são obrigatórios (fail-fast via `${VAR:?}`)
- [AC-22] PostgreSQL **não exposto** em produção (apenas rede interna do compose)

### Geral
- [AC-23] Suite de testes verde (193+ testes)

## Out of Scope

- OAuth2 / OIDC integration
- Single sign-out
- Token revocation por admin
- Cleanup automático de refresh tokens expirados (M1 do code review)
- Kubernetes manifests
- Helm charts

## Open Questions

(Resolvidas)

- ~~Refresh token: UUID ou bytes aleatórios?~~ → UUID (suficiente para MVP;
  M2 do code review pode evoluir para `SecureRandom` 256 bits)
- ~~Refresh token retornado como header ou body?~~ → body JSON
  (compatível com clientes web e mobile, evita CORS)

## Constitutional Compliance

- ✓ Article II (Layers): adapter na infra, domínio limpo
- ✓ Article III (Records): `RefreshRequest`, `TokenResponse`, `RefreshResult`
- ✓ Article V (Security): rotation, claim `token_type`, rate limit, validação
- ✓ Article VII (Persistence): nova migration V3 vendor-specific, FK
- ✓ Article XI (Container): multi-stage, non-root, fail-fast vars
