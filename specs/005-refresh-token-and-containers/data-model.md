# Data Model: Refresh Token

**Spec:** [spec.md](./spec.md)
**Plan:** [plan.md](./plan.md)

## Entity: RefreshToken

```java
@Entity
@Table(name = "refresh_token", indexes = {
    @Index(name = "idx_refresh_token_token",   columnList = "token", unique = true),
    @Index(name = "idx_refresh_token_user_id", columnList = "user_id")
})
public class RefreshToken {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(Types.BINARY)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false)
    @JdbcTypeCode(Types.BINARY)
    private UUID userId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;
}
```

## Schema (H2)

```sql
CREATE TABLE refresh_token (
    id          UUID            NOT NULL,
    token       VARCHAR(255)    NOT NULL,
    user_id     UUID            NOT NULL,
    expires_at  TIMESTAMP       NOT NULL,
    used        BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);
```

## Schema (PostgreSQL)

Idêntico ao H2, mas `id` e `user_id` como **BYTEA** (consistente com V1 do PG):

```sql
CREATE TABLE refresh_token (
    id          BYTEA           NOT NULL,
    token       VARCHAR(255)    NOT NULL,
    user_id     BYTEA           NOT NULL,
    ...
);
```

## Relationships

```
users (1) ──< (N) refresh_token
```

- Um usuário pode ter múltiplos refresh tokens ativos (login em múltiplos dispositivos)
- FK `refresh_token.user_id → users(id)` impede tokens órfãos
- Sem cascade — delete de user não deleta tokens (intencional para audit)

## Constraints

| Constraint | Tipo | Descrição |
|------------|------|-----------|
| pk_refresh_token | PRIMARY KEY | id |
| uq_refresh_token_token | UNIQUE | token (lookup rápido) |
| fk_refresh_token_user | FOREIGN KEY | user_id → users.id |
| idx_refresh_token_user_id | INDEX | acelera lookup por user |

## Lifecycle

```
[CREATE]    Login bem-sucedido      → INSERT used=false, expiresAt=now+7d
[USE]       POST /auth/refresh      → SELECT WHERE token=? AND used=false
                                      AND expiresAt > now
[ROTATE]    refresh válido          → UPDATE used=true (token velho)
                                    + INSERT (token novo)
[CLEANUP]   (futuro M1)             → DELETE WHERE used=true OR expiresAt < now
```

## Audit Trail

Atualmente RefreshToken NÃO estende `AbstractEntity` (sem `dateCreatedAt`,
`dateUpdatedAt`). Code review identificou como **MAJOR (M4)** — pendente
para follow-up.
