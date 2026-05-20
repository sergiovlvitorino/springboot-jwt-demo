# Quickstart: Refresh Token & Containerization

**Spec:** [spec.md](./spec.md)

## Prerequisites

- Java 21
- Maven 3.9+
- Docker + Docker Compose (para testes do T13)
- `JWT_SECRET` exportado (mínimo 32 caracteres)

## Quickstart 1 — Refresh Token (sem Docker)

```bash
# 1. Subir aplicação em dev (H2 + Flyway)
export JWT_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 2. Login (em outro terminal)
curl -i -X POST http://localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin@example.com","password":"Test@1234"}'

# Resposta:
# HTTP/1.1 200
# Authorization: Bearer eyJ...        ← access token
# {"accessToken":"eyJ...","refreshToken":"550e8400-..."}

# 3. Salvar refresh token
REFRESH_TOKEN="550e8400-..."

# 4. Refresh (recebe novo par)
curl -i -X POST http://localhost:8080/auth/refresh \
  -H 'Content-Type: application/json' \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"

# 5. Tentar reusar mesmo refresh token → 401
curl -i -X POST http://localhost:8080/auth/refresh \
  -H 'Content-Type: application/json' \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"

# Resposta esperada:
# HTTP/1.1 401
# [{"errorCode":"INVALID_REFRESH_TOKEN","field":null,"message":"..."}]
```

## Quickstart 2 — Docker Compose (prod-like)

```bash
# 1. Definir secrets
export DB_PASSWORD="$(openssl rand -base64 24)"
export JWT_SECRET="$(openssl rand -base64 48)"

# 2. Subir tudo
docker compose up -d

# 3. Aguardar health check
docker compose ps

# 4. Testar login
curl -i -X POST http://localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin@example.com","password":"Test@1234"}'
```

## Quickstart 3 — Dev local com PostgreSQL real

```bash
# 1. Subir apenas PostgreSQL
docker compose -f docker-compose.dev.yml up -d

# 2. Rodar app localmente apontando para o postgres do container
export JWT_SECRET="dev-secret-with-at-least-32-characters-long!"
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/springjwt"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="postgres"
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Cenários de teste manual

| Cenário | Passos | Esperado |
|---------|--------|----------|
| Happy path refresh | Login → guarda refresh → POST /auth/refresh | 200 + novo par |
| Reuso de refresh | Login → refresh → mesmo refresh de novo | 401 INVALID_REFRESH_TOKEN |
| Refresh expirado | Login → setar `expiresAt` no passado via SQL → refresh | 401 |
| Refresh sem body | POST /auth/refresh sem body | 400 |
| Refresh com token vazio | `{"refreshToken":""}` | 400 (NotBlank) |
| Rate limit no refresh | Loop 100x POST /auth/refresh | 429 (após maxAttempts) |
| User bloqueado | Setar `accountLocked=true` no admin → tentar refresh | 401 |
