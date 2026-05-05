# Feature: Security Hardening

**Status:** Implemented
**Author:** sergio
**Created:** 2026-04-13
**Spec ID:** 002
**Sprint:** 2

## Problem Statement

Análise de segurança identificou múltiplas lacunas:
- Authority `USER_SAVE` usada para delete (semanticamente incorreta, viola princípio
  de menor privilégio)
- Rate limiter usa `request.getRemoteAddr()` — atrás de proxy reverso (nginx, ALB),
  todos os requests teriam o mesmo IP, anulando a proteção
- Faltam headers CSP e HSTS (recomendação OWASP Defense-in-Depth)
- Sem scan automático de vulnerabilidades em dependências

## User Stories

- Como **administrador de segurança**, quero authorities granulares (separar
  delete de save), para conceder permissões mínimas necessárias.
- Como **operador atrás de load balancer**, quero rate limit baseado no IP real
  do cliente (não do proxy), para que a proteção funcione em produção.
- Como **auditor**, quero headers HTTP que indiquem boa postura de segurança
  (HSTS, CSP), para passar checks de compliance.
- Como **mantenedor**, quero scan automatizado de CVEs nas dependências, para
  ser notificado quando uma vulnerabilidade conhecida afeta o projeto.

## Acceptance Criteria

- [AC-1] Authority `USER_DELETE` existe e é usada em `DELETE /rest/user/{id}`
- [AC-2] Usuário com `USER_SAVE` mas sem `USER_DELETE` recebe 403 ao tentar deletar
- [AC-3] `@EnableMethodSecurity` ativo (sem ele, `@PreAuthorize` é ignorado)
- [AC-4] `LoginRateLimitFilter` lê `X-Forwarded-For` quando presente
- [AC-5] Quando XFF tem múltiplos IPs (`1.2.3.4, 10.0.0.1`), usa o primeiro
- [AC-6] Sem XFF, usa `getRemoteAddr()` (fallback)
- [AC-7] Resposta HTTP contém headers: `Strict-Transport-Security`, `Content-Security-Policy`
- [AC-8] HSTS com `max-age=31536000; includeSubDomains`
- [AC-9] CSP com `default-src 'none'` (API REST pura)
- [AC-10] CI executa `mvn dependency-check:check`
- [AC-11] Plugin OWASP configurado para falhar build em CVSS ≥ 7
- [AC-12] Job de dependency-check é separado do build principal (`continue-on-error: true`)

## Out of Scope

- Bloqueio automático de conta após N tentativas (existe `accountLocked` mas
  sem trigger automático)
- WAF, DDoS protection (responsabilidade de infra)
- Pen testing externo

## Open Questions

(Resolvidas durante implementação)

## Constitutional Compliance

- ✓ Article V (Security): authorities granulares, rate limit por IP real, headers
- ✓ Article X (CI/CD): OWASP scan no pipeline
- ⚠ Article V item 8: novo `USER_DELETE` exige adição ao seed e ao role ADMIN
