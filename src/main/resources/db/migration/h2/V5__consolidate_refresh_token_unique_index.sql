-- V5__consolidate_refresh_token_unique_index.sql (H2)
--
-- Rationale (two issues addressed):
--
-- (a) UNIQUE duplication [ALTA-1]:
--     V4 created both a CONSTRAINT UNIQUE (uq_refresh_token_token_hash) AND a
--     CREATE UNIQUE INDEX (idx_refresh_token_token_hash) on the same column.
--     The RefreshToken entity carries @Index(name="idx_refresh_token_token_hash",
--     unique=true), so Hibernate ddl-auto=validate expects that named unique index.
--     The explicit CONSTRAINT creates a second, redundant index under a different
--     internal name. We drop the constraint and keep only the named unique index,
--     aligning schema with the entity mapping.
--
-- (b) Pre-condition for prod safety [ALTA-2]:
--     V4 added token_hash VARCHAR(64) NOT NULL DEFAULT ''. On a table with
--     pre-existing rows the empty-string default would cause all legacy rows to
--     collide on the UNIQUE constraint, aborting the migration. Refresh tokens are
--     ephemeral (7-day TTL); any pre-V4 tokens stored as plaintext were already
--     invalidated by the column rename in V4. We DELETE defensively so the unique
--     index is always satisfiable, even during a rolling deployment.
--
-- Constitucao Art. VII: V1-V4 are immutable; new changes go in a new version.

-- Step 1: clear the table defensively (see rationale b above)
DELETE FROM refresh_token;

-- Step 2: drop the redundant explicit UNIQUE constraint (see rationale a above)
ALTER TABLE refresh_token DROP CONSTRAINT IF EXISTS uq_refresh_token_token_hash;
