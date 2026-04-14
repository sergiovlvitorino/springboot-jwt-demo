-- V3__refresh_tokens.sql (H2)
-- Refresh token table for token rotation

CREATE TABLE refresh_token (
    id          UUID            NOT NULL,
    token       VARCHAR(255)    NOT NULL,
    user_id     UUID            NOT NULL,
    expires_at  TIMESTAMP       NOT NULL,
    used        BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_token UNIQUE (token)
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);
