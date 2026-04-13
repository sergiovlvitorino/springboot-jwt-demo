-- V1__baseline.sql (PostgreSQL)
-- Baseline schema migration — PostgreSQL dialect (prod profile)
-- UUID columns declared as BYTEA to match @JdbcTypeCode(java.sql.Types.BINARY)

CREATE TABLE authority (
    id              BYTEA        NOT NULL,
    name            VARCHAR(255),
    CONSTRAINT pk_authority PRIMARY KEY (id)
);

CREATE TABLE role (
    id              BYTEA        NOT NULL,
    name            VARCHAR(255),
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE role_authority (
    role_id         BYTEA        NOT NULL,
    authority_id    BYTEA        NOT NULL,
    CONSTRAINT pk_role_authority PRIMARY KEY (role_id, authority_id),
    CONSTRAINT fk_role_authority_role      FOREIGN KEY (role_id)      REFERENCES role(id),
    CONSTRAINT fk_role_authority_authority FOREIGN KEY (authority_id) REFERENCES authority(id)
);

CREATE TABLE users (
    id                  BYTEA           NOT NULL,
    name                VARCHAR(255)    NOT NULL,
    email               VARCHAR(255)    NOT NULL,
    password            VARCHAR(255)    NOT NULL,
    enabled             BOOLEAN,
    account_locked      BOOLEAN         NOT NULL DEFAULT FALSE,
    role_id             BYTEA           NOT NULL,
    date_created_at     TIMESTAMP       NOT NULL,
    date_updated_at     TIMESTAMP,
    date_disabled_at    TIMESTAMP,
    user_id_created_at  BYTEA,
    user_id_updated_at  BYTEA,
    user_id_disabled_at BYTEA,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_role  FOREIGN KEY (role_id) REFERENCES role(id)
);
