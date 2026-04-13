-- V1__baseline.sql (H2)
-- Baseline schema migration — H2 dialect (dev / test profiles)
-- UUID columns declared as BINARY(16) to match @JdbcTypeCode(java.sql.Types.BINARY)

CREATE TABLE authority (
    id              BINARY(16)   NOT NULL,
    name            VARCHAR(255),
    CONSTRAINT pk_authority PRIMARY KEY (id)
);

CREATE TABLE role (
    id              BINARY(16)   NOT NULL,
    name            VARCHAR(255),
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE role_authority (
    role_id         BINARY(16)   NOT NULL,
    authority_id    BINARY(16)   NOT NULL,
    CONSTRAINT pk_role_authority PRIMARY KEY (role_id, authority_id),
    CONSTRAINT fk_role_authority_role      FOREIGN KEY (role_id)      REFERENCES role(id),
    CONSTRAINT fk_role_authority_authority FOREIGN KEY (authority_id) REFERENCES authority(id)
);

CREATE TABLE users (
    id                  BINARY(16)      NOT NULL,
    name                VARCHAR(255)    NOT NULL,
    email               VARCHAR(255)    NOT NULL,
    password            VARCHAR(255)    NOT NULL,
    enabled             BOOLEAN,
    account_locked      BOOLEAN         NOT NULL DEFAULT FALSE,
    role_id             BINARY(16)      NOT NULL,
    date_created_at     TIMESTAMP       NOT NULL,
    date_updated_at     TIMESTAMP,
    date_disabled_at    TIMESTAMP,
    user_id_created_at  BINARY(16),
    user_id_updated_at  BINARY(16),
    user_id_disabled_at BINARY(16),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_role  FOREIGN KEY (role_id) REFERENCES role(id)
);
