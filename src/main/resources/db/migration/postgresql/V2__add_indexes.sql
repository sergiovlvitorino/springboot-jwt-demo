-- V2__add_indexes.sql (PostgreSQL)
-- Indexes on frequently filtered columns used by ExampleMatcher queries

-- users.enabled: filtered in UserService.findAll() and count() via ExampleMatcher
CREATE INDEX IF NOT EXISTS idx_users_enabled ON users (enabled);

-- users.name: filtered in UserService.findAll() and count() via ExampleMatcher
CREATE INDEX IF NOT EXISTS idx_users_name ON users (name);

-- role.name: filtered in RoleService.findAll() and count() via ExampleMatcher
CREATE INDEX IF NOT EXISTS idx_role_name ON role (name);
