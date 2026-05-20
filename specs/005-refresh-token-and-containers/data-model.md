# Data Model: Refresh Token

**Spec:** [spec.md](./spec.md)
**Plan:** [plan.md](./plan.md)

> **Atualizado em 2026-05-19** para refletir as decisões do ADR-0005:
> token raw substituído por `token_hash` (SHA-256); campos de auditoria
> `created_at` / `used_at` adicionados; lifecycle atualizado para reuse → revogação
> em cadeia.

## Entity: RefreshToken

```java
@Entity
@Table(name = "refresh_token", indexes = {
    @Index(name = "idx_refresh_token_hash",    columnList = "token_hash", unique = true),
    @Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_token_user_used", columnList = "user_id, used")
})
public class RefreshToken {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(Types.BINARY)
    private UUID id;

    /** SHA-256 do token raw (UUID). O cliente detém o valor raw; o banco armazena apenas o hash. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    @JdbcTypeCode(Types.BINARY)
    private UUID userId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    /** Quando o token foi emitido (audit trail). */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Quando o token foi consumido/revogado (null enquanto não usado). */
    @Column(name = "used_at")
    private LocalDateTime usedAt;
}
```

## Schema (H2) — V4

```sql
CREATE TABLE refresh_token (
    id          UUID            NOT NULL,
    token_hash  VARCHAR(64)     NOT NULL,
    user_id     UUID            NOT NULL,
    expires_at  TIMESTAMP       NOT NULL,
    used        BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL,
    used_at     TIMESTAMP,
    CONSTRAINT pk_refresh_token     PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_refresh_token_user_id   ON refresh_token (user_id);
CREATE INDEX idx_refresh_token_user_used ON refresh_token (user_id, used);
```

> **Migration:** V4 adiciona `token_hash`, `created_at`, `used_at` e remove a coluna
> `token` original. Irreversível após merge — tokens existentes são invalidados na
> transição (usuários precisam refazer login).

## Schema (PostgreSQL) — V4

Idêntico ao H2, mas `id` e `user_id` como **BYTEA** (consistente com V1 do PG):

```sql
CREATE TABLE refresh_token (
    id          BYTEA           NOT NULL,
    token_hash  VARCHAR(64)     NOT NULL,
    user_id     BYTEA           NOT NULL,
    expires_at  TIMESTAMP       NOT NULL,
    used        BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL,
    used_at     TIMESTAMP,
    CONSTRAINT pk_refresh_token      PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_refresh_token_user_id   ON refresh_token (user_id);
CREATE INDEX idx_refresh_token_user_used ON refresh_token (user_id, used);
```

## Relationships

```
users (1) ──< (N) refresh_token
```

- Um usuário pode ter múltiplos refresh tokens ativos (login em múltiplos dispositivos).
- FK `refresh_token.user_id → users.id` impede tokens órfãos.
- Sem cascade — delete de user não deleta tokens (intencional para audit trail).

## Constraints

| Constraint | Tipo | Descrição |
|------------|------|-----------|
| pk_refresh_token | PRIMARY KEY | id |
| uq_refresh_token_hash | UNIQUE | token_hash (lookup rápido por hash SHA-256) |
| fk_refresh_token_user | FOREIGN KEY | user_id → users.id |
| idx_refresh_token_user_id | INDEX | lookup por user |
| idx_refresh_token_user_used | INDEX | revogação em cadeia (`WHERE user_id=? AND used=false`) |

## Hashing

O token raw (UUID v4, 128 bits de entropia) é nunca persistido.
`RefreshTokenHasher.hash(rawToken)` aplica SHA-256 e retorna hex string de 64 chars.

```
token_hash = SHA-256(rawToken).toHexString()
```

Lookup: `RefreshTokenRepository.findByTokenHash(hash)` — índice único garante O(log n).

## Lifecycle

```
[CREATE]    Login bem-sucedido      → INSERT token_hash=sha256(raw), used=false,
                                      created_at=now, used_at=null, expiresAt=now+7d
                                      Cliente recebe rawToken (UUID).

[VALIDATE]  POST /auth/refresh      → hash = sha256(rawToken apresentado)
                                      SELECT WHERE token_hash = hash
                                        → not found   → 401 InvalidRefreshTokenException
                                        → used=true   → 401 (replay detection)
                                        → expiresAt < now → 401 (expired)

[ROTATE]    refresh válido          → UPDATE used=true, used_at=now (token velho)
                                      UPDATE SET used=true, used_at=now
                                        WHERE user_id=? AND used=false AND id != tokenId
                                        (revogação em cadeia de tokens anteriores)
                                      INSERT novo token_hash, created_at=now
                                      Cliente recebe novo rawToken.

[CLEANUP]   (futuro M1)             → DELETE WHERE used=true OR expiresAt < now
```

## Audit Trail

Campos `created_at` e `used_at` fornecem rastreabilidade completa do ciclo de vida:

| Campo | Preenchido em | Descrição |
|-------|--------------|-----------|
| `created_at` | INSERT | Quando o token foi emitido (login ou rotação) |
| `used_at` | UPDATE (rotate/revoke) | Quando foi consumido ou revogado em cadeia |
| `used` | UPDATE | Flag de uso — índice para queries de revogação em cadeia |

> **Nota operacional:** monitorar `used_at IS NULL AND expires_at < now` para identificar
> tokens expirados não consumidos (sessões abandonadas). Purge periódico é necessário
> para controlar crescimento da tabela.
