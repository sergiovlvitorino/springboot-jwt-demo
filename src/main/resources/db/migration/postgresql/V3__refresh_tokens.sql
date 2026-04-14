-- V3__refresh_tokens.sql (PostgreSQL)
-- Refresh token table for token rotation
-- UUID columns as BYTEA to match @JdbcTypeCode(java.sql.Types.BINARY)

CREATE TABLE refresh_token (
    id          BYTEA           NOT NULL,
    token       VARCHAR(255)    NOT NULL,
    user_id     BYTEA           NOT NULL,
    expires_at  TIMESTAMP       NOT NULL,
    used        BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);
