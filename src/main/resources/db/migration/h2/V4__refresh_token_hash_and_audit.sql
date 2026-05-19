-- V4__refresh_token_hash_and_audit.sql (H2)
-- SEC-1: Replace plaintext token with SHA-256 hash (64 hex chars)
-- SEC-5: Add audit trail (created_at, used_at) and chain revocation support

ALTER TABLE refresh_token DROP CONSTRAINT IF EXISTS uq_refresh_token_token;
ALTER TABLE refresh_token DROP CONSTRAINT IF EXISTS idx_refresh_token_token;
ALTER TABLE refresh_token DROP COLUMN IF EXISTS token;

ALTER TABLE refresh_token ADD COLUMN token_hash VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE refresh_token ADD CONSTRAINT uq_refresh_token_token_hash UNIQUE (token_hash);

ALTER TABLE refresh_token ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE refresh_token ADD COLUMN used_at TIMESTAMP;

DROP INDEX IF EXISTS idx_refresh_token_token;
CREATE UNIQUE INDEX IF NOT EXISTS idx_refresh_token_token_hash ON refresh_token (token_hash);
