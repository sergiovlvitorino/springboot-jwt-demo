# Data Model: Database Migrations & Performance

**Spec:** [spec.md](./spec.md)
**Plan:** [plan.md](./plan.md)
**Status:** Backfill retroativo (artefato opcional documentado a posteriori)

> Escopo: este documento descreve o schema **no momento da spec 003**. A
> entidade `RefreshToken` foi adicionada posteriormente pela spec 005 e
> portanto **nao** aparece aqui — sua documentacao vive em
> `specs/005-refresh-token-and-containers/data-model.md`.

## Visao geral

A spec 003 nao introduziu novas entidades — formalizou o schema existente
sob controle do Flyway, separou migrations por dialeto (H2 vs PostgreSQL),
adicionou indices em colunas filtradas e automatizou auditoria via
`@PrePersist` / `@PreUpdate`.

## Entidades

### Authority

Granularidade de permissao usada por `@PreAuthorize`.

| Campo | Tipo Java | Coluna SQL (H2 / PG) | Nullable | Default | Observacao |
|-------|-----------|----------------------|----------|---------|------------|
| `id` | `UUID` | `UUID` / `BYTEA` | NOT NULL | gerado | PK; `@JdbcTypeCode(BINARY)` |
| `name` | `String` | `VARCHAR(255)` | sim | — | ex.: `USER_SAVE`, `USER_DELETE`, `USER_RETRIEVE`, `ROLE_RETRIEVE` |

### Role

Agrupa autoridades; cada usuario tem **uma** role.

| Campo | Tipo Java | Coluna SQL | Nullable | Default | Observacao |
|-------|-----------|------------|----------|---------|------------|
| `id` | `UUID` | `UUID` / `BYTEA` | NOT NULL | gerado | PK; `@JdbcTypeCode(BINARY)` |
| `name` | `String` | `VARCHAR(255)` | sim | — | ex.: `ADMIN` |
| `authorities` | `List<Authority>` | (join table) | — | — | `@ManyToMany` via `role_authority` |
| `users` | `List<User>` | (inverse) | — | — | `@OneToMany(mappedBy = "role")`, `cascade = DETACH` |

### role_authority (join table)

Tabela associativa pura, sem entidade JPA propria.

| Campo | Tipo SQL (H2 / PG) | Nullable | FK |
|-------|--------------------|----------|----|
| `role_id` | `UUID` / `BYTEA` | NOT NULL | `role(id)` |
| `authority_id` | `UUID` / `BYTEA` | NOT NULL | `authority(id)` |

PK composta: `(role_id, authority_id)`.

### User

Entidade principal de identidade. Estende `AbstractEntity` (auditoria).

| Campo | Tipo Java | Coluna SQL | Nullable | Default | Observacao |
|-------|-----------|------------|----------|---------|------------|
| `id` | `UUID` | `UUID` / `BYTEA` | NOT NULL | gerado | PK; `@JdbcTypeCode(BINARY)` |
| `name` | `String` | `VARCHAR(255)` | NOT NULL | — | indice `idx_users_name` (V2) |
| `email` | `String` | `VARCHAR(255)` | NOT NULL | — | UNIQUE `uq_users_email` |
| `password` | `String` | `VARCHAR(255)` | NOT NULL | — | BCrypt; `@JsonIgnore` |
| `enabled` | `Boolean` | `BOOLEAN` | sim | — | soft-delete (Article V item 9); indice `idx_users_enabled` (V2) |
| `accountLocked` | `Boolean` | `BOOLEAN` | NOT NULL | `FALSE` | flag manual (sem trigger automatico — fora de escopo) |
| `role_id` | FK -> Role | `UUID` / `BYTEA` | NOT NULL | — | `@ManyToOne(EAGER)` |
| `dateCreatedAt` | `LocalDateTime` | `TIMESTAMP` | NOT NULL | `@PrePersist` | herdado de `AbstractEntity`; nunca atualizado |
| `dateUpdatedAt` | `LocalDateTime` | `TIMESTAMP` | sim | `@PreUpdate` | herdado; setado automaticamente |
| `dateDisabledAt` | `LocalDateTime` | `TIMESTAMP` | sim | — | preenchido por `UserService.disable()` |
| `userIdCreatedAt` | `UUID` | `UUID` / `BYTEA` | sim | — | autor da criacao; nao-atualizavel |
| `userIdUpdatedAt` | `UUID` | `UUID` / `BYTEA` | sim | — | autor da ultima atualizacao |
| `userIdDisabledAt` | `UUID` | `UUID` / `BYTEA` | sim | — | autor do disable |

## Diagrama de relacionamentos

```
              +-----------+        +-----------------+        +-----------+
              | authority |--------|  role_authority |--------|   role    |
              +-----------+  N..N  +-----------------+  N..N  +-----------+
              | id (PK)   |        | role_id      FK |        | id (PK)   |
              | name      |        | authority_id FK |        | name      |
              +-----------+        +-----------------+        +-----------+
                                                                    |
                                                                  1 |
                                                                    |
                                                                  N v
                                                              +-----------+
                                                              |   users   |
                                                              +-----------+
                                                              | id (PK)   |
                                                              | name      |
                                                              | email UQ  |
                                                              | password  |
                                                              | enabled   |
                                                              | account_  |
                                                              |   locked  |
                                                              | role_id FK|
                                                              | audit *   |
                                                              +-----------+
```

Cardinalidade resumida:

| Relacao | Cardinalidade | Cascade | Observacao |
|---------|---------------|---------|------------|
| `Role` ←→ `Authority` | N..N (via `role_authority`) | `DETACH` | EAGER em `Role.authorities` para popular GrantedAuthorities no contexto de seguranca |
| `User` → `Role` | N..1 (FK obrigatoria) | nenhum | EAGER (necessario na construcao do `Authentication`) |
| `Role` → `User` | 1..N (inverse) | `DETACH` | Apenas leitura logica; nao usado em escrita |

## Decisao: UUID como BINARY (Article VII item 6)

Todas as PKs e FKs sao `UUID` em Java, mapeadas via:

```java
@Id
@GeneratedValue
@UuidGenerator
@JdbcTypeCode(java.sql.Types.BINARY)
private UUID id;
```

- **PostgreSQL**: armazenado como `BYTEA` (16 bytes brutos). Optamos por
  nao usar o tipo nativo `UUID` do PG para manter o mesmo binding JDBC
  em todos os dialetos suportados pelo projeto.
- **H2**: armazenado como `UUID` (tipo nativo do H2, que internamente
  tambem usa 16 bytes). Hibernate aceita o binding `BINARY` contra a
  coluna `UUID` do H2 sem coercao.
- **Trade-off aceito**: perdemos a legibilidade da coluna em consultas
  ad-hoc (precisamos converter `BYTEA` → UUID textual no PG). Em troca,
  ganhamos consistencia de mapeamento e evitamos o problema classico de
  tipos divergentes entre dev (H2) e prod (PG) — que originou o risco
  registrado em `plan.md` ("Schema validation falha em testes").

## Decisao: migrations vendor-specific

Adotamos diretorios separados em `src/main/resources/db/migration/`:

- `db/migration/h2/` — usado por profiles `dev` e `test`
- `db/migration/postgresql/` — usado pelo profile `prod`

Selecao via `spring.flyway.locations` no `application-<profile>.properties`.

**Por que nao placeholders Flyway?** Os dialetos divergem em pontos que
placeholders nao cobrem bem:
- Tipo da coluna UUID (`UUID` vs `BYTEA`)
- Quoting de identificadores (PG case-sensitive entre aspas)
- Dialetos de `IF NOT EXISTS` para indices (suportado em ambos hoje, mas
  historicamente divergente)

Manter dois arquivos paralelos, identicos em estrutura e `version`, e
mais explicito e auditavel do que uma unica migration parametrizada.

## Auditoria automatica (Article VII item 4)

Centralizada em `AbstractEntity` (`@MappedSuperclass`):

```java
@PrePersist
public void onPrePersist() {
    this.dateCreatedAt = LocalDateTime.now();
}

@PreUpdate
public void onPreUpdate() {
    this.dateUpdatedAt = LocalDateTime.now();
}
```

- `dateCreatedAt`: `@Column(nullable = false, updatable = false)` —
  imutavel apos insert.
- `dateUpdatedAt`: nullable; setado a cada UPDATE pelo callback.
- `userIdCreatedAt`: `@Column(updatable = false)` — coerencia com
  `dateCreatedAt` (autor original nao muda).

Antes da spec 003, `UserService.update()` setava `dateUpdatedAt` manualmente,
o que era propenso a erro (qualquer service novo precisava lembrar). Pos
spec 003, o callback JPA garante o registro mesmo em paths que esquecam.

## Soft-delete (Article V item 9)

DELETE fisico nao e usado para `User`. O endpoint `DELETE /rest/user/{id}`
chama `UserService.disable()`, que:

1. Setta `enabled = false`
2. Setta `dateDisabledAt = now()`
3. Setta `userIdDisabledAt = <ator>`
4. `save(user)` — UPDATE, nao DELETE

Listagens filtram por `enabled` (com indice `idx_users_enabled` adicionado
em V2) para excluir registros desativados quando aplicavel.

## Migrations criadas pela spec 003

| Versao | Arquivo | Proposito |
|--------|---------|-----------|
| V1 | `db/migration/h2/V1__baseline.sql` | Cria `authority`, `role`, `role_authority`, `users` (UUID nativo) |
| V1 | `db/migration/postgresql/V1__baseline.sql` | Mesmo schema com `BYTEA` para colunas UUID |
| V2 | `db/migration/h2/V2__add_indexes.sql` | Indices `idx_users_enabled`, `idx_users_name`, `idx_role_name` |
| V2 | `db/migration/postgresql/V2__add_indexes.sql` | Mesmos indices em PG |

> V3 (`refresh_tokens`) NAO pertence a esta spec — foi criada pela spec 005.

## Constraints e indices (snapshot pos-spec 003)

| Tabela | Constraint / Indice | Tipo | Colunas | Origem |
|--------|---------------------|------|---------|--------|
| `authority` | `pk_authority` | PK | `id` | V1 |
| `role` | `pk_role` | PK | `id` | V1 |
| `role_authority` | `pk_role_authority` | PK | `(role_id, authority_id)` | V1 |
| `role_authority` | `fk_role_authority_role` | FK | `role_id -> role(id)` | V1 |
| `role_authority` | `fk_role_authority_authority` | FK | `authority_id -> authority(id)` | V1 |
| `users` | `pk_users` | PK | `id` | V1 |
| `users` | `uq_users_email` | UNIQUE | `email` | V1 |
| `users` | `fk_users_role` | FK | `role_id -> role(id)` | V1 |
| `users` | `idx_users_enabled` | INDEX | `enabled` | V2 |
| `users` | `idx_users_name` | INDEX | `name` | V2 |
| `role` | `idx_role_name` | INDEX | `name` | V2 |

## Configuracao por profile

| Profile | `spring.flyway.locations` | `spring.jpa.hibernate.ddl-auto` |
|---------|--------------------------|--------------------------------|
| `dev` | `classpath:db/migration/h2` | `validate` |
| `test` | `classpath:db/migration/h2` | `validate` |
| `prod` | `classpath:db/migration/postgresql` | `validate` |

`validate` em todos os profiles garante paridade dev/prod e elimina
mutacao acidental de schema por Hibernate em runtime (Article VII).
