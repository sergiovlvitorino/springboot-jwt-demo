# ADR-0004: Multi-Stage Dockerfile with Non-Root Runtime

**Status:** Accepted
**Date:** 2026-04-14

## Context

A spec `005-refresh-token-and-containers` incluiu containerização produção-ready. Três
preocupações simultâneas direcionaram a decisão:

1. **Tamanho da imagem**: imagens com Maven + JDK completo passam de 600 MB. Indesejável
   para deploy/registry e aumenta superfície de ataque.
2. **Princípio do menor privilégio**: rodar JVM como `root` dentro do container é prática
   ruim — qualquer escape ou exploração de bibliotecas sobe direto para `uid 0`.
3. **Paridade dev/prod e fail-fast em config**: o mesmo `docker-compose.yml` precisa
   subir Postgres + app no laptop do dev e em ambiente alvo, sem default inseguro para
   secrets (`DB_PASSWORD`, `JWT_SECRET`).

O Article XI da constituição já fixava as regras (multi-stage, runtime `21-jre`, non-root,
fail-fast em env vars sensíveis, Postgres não exposto em prod). Este ADR registra o
**como** dessa decisão.

## Decision

`Dockerfile` com dois estágios:

- **Stage 1 (`build`)**: `maven:3.9-eclipse-temurin-21`. Copia `pom.xml`, baixa
  dependências (`mvn dependency:go-offline -B` — aproveita cache de layer), copia `src/`
  e roda `mvn package -DskipTests -B`.
- **Stage 2 (`runtime`)**: `eclipse-temurin:21-jre` (apenas JRE, sem ferramentas de
  build).
  - Cria grupo+usuário system `appgroup`/`appuser` sem home.
  - Copia o JAR do stage `build` (`COPY --from=build`).
  - `chown` recursivo + `USER appuser`.
  - `ENTRYPOINT` com flags JVM container-aware (`-XX:+UseContainerSupport`,
    `-XX:MaxRAMPercentage=75.0`, `-Djava.security.egd=file:/dev/./urandom`).

`docker-compose.yml` complementa:

- Serviço `db` (postgres:16-alpine) com `POSTGRES_PASSWORD: ${DB_PASSWORD:?...}` —
  fail-fast se a env var não estiver setada.
- Serviço `app` com `JWT_SECRET: ${JWT_SECRET:?...}` e `SPRING_DATASOURCE_PASSWORD:
  ${DB_PASSWORD:?...}` — também fail-fast.
- `db.ports: ["5432"]` (sem mapeamento para host) — Postgres NÃO exposto na rede do host
  em produção; comunicação só via rede interna `backend`.
- `healthcheck` em ambos os serviços; `depends_on: condition: service_healthy` garante
  ordem.
- `deploy.resources.limits.memory: 512M` casa com a flag `MaxRAMPercentage` da JVM.

`docker-compose.dev.yml` separado expõe Postgres no host para conexão a partir do
`./mvnw spring-boot:run` local — **não** usado em prod.

## Consequences

### Positivas
- **Imagem runtime pequena**: apenas JRE + JAR; tooling de build não vai para registry.
- **Superfície de ataque reduzida**: container rodando como `appuser` (uid não-zero, sem
  home, sem shell login).
- **Configuração inválida falha cedo**: `docker compose up` aborta imediatamente se
  `DB_PASSWORD` ou `JWT_SECRET` não estiverem no ambiente — impossível subir prod com
  default fraco por engano.
- **Paridade dev/prod**: o mesmo Dockerfile produz a imagem usada localmente e em alvo.
- **JVM container-aware**: `MaxRAMPercentage=75` respeita o limite imposto pelo
  orquestrador, evitando OOM por heuristicas de memory ergonomics legadas.

### Negativas / Trade-offs aceitos
- **Build local mais lento na primeira execução** (cache de dependências Maven precisa
  ser populado); subsequentes são rápidos enquanto `pom.xml` não muda.
- **Dois compose files** (`docker-compose.yml` + `docker-compose.dev.yml`) introduzem
  uma pequena curva de aprendizado para novos devs. Aceito em troca de não vazar
  publicação de Postgres em prod.
- Imagem base `eclipse-temurin:21-jre` (não Alpine) — escolhida pela compatibilidade com
  glibc e drivers JDBC; custo de ~50 MB extras em troca de menor risco de incidentes em
  prod.

### Neutras / Implicações operacionais
- Atualizações da imagem base exigem rebuild + redeploy (mesmo sem mudança de código).
  Pipeline futuro DEVE rodar dependency-check sobre a imagem final, não apenas sobre
  dependências Maven.
- A flag `-Djava.security.egd=file:/dev/./urandom` é aceitável em Linux moderno; revisar
  se rodar em ambientes onde `/dev/urandom` não tem entropia garantida.
