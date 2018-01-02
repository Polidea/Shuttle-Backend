SET client_encoding = 'UTF8';
SET default_tablespace = '';
SET TIME ZONE 'UTC';

CREATE TABLE store_users
(
  id          SERIAL                 NOT NULL,
  email       CHARACTER VARYING(255) NOT NULL,
  name        CHARACTER VARYING(255) NOT NULL,
  avatar_href CHARACTER VARYING(255),
  is_deleted  BOOLEAN                NOT NULL DEFAULT FALSE,
  CONSTRAINT store_users_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX store_users_ukey_email
  ON store_users (email)
  WHERE (is_deleted IS FALSE);

CREATE TABLE push_tokens
(
  id       SERIAL                 NOT NULL,
  owner_id INTEGER                NOT NULL,
  platform CHARACTER VARYING(255) NOT NULL,
  value    CHARACTER VARYING(255) NOT NULL,
  CONSTRAINT push_tokens_pkey PRIMARY KEY (id),
  CONSTRAINT push_tokens_fkey_owner FOREIGN KEY (owner_id) REFERENCES store_users (id) MATCH SIMPLE,
  CONSTRAINT push_tokens_ukey_owner_and_platform_and_value UNIQUE (owner_id, platform, value)
);

CREATE TABLE tokens
(
  id                 SERIAL                 NOT NULL,
  value              TEXT                   NOT NULL,
  owner_id           INTEGER                NOT NULL,
  type               CHARACTER VARYING(127) NOT NULL,
  creation_timestamp TIMESTAMP              NOT NULL,
  CONSTRAINT tokens_pkey PRIMARY KEY (id),
  CONSTRAINT tokens_fkey_owner FOREIGN KEY (owner_id) REFERENCES store_users (id) MATCH SIMPLE
);

CREATE TABLE projects
(
  id         SERIAL                 NOT NULL,
  name       CHARACTER VARYING(255) NOT NULL,
  icon_href  VARCHAR(255),
  is_deleted BOOLEAN                NOT NULL DEFAULT FALSE,
  CONSTRAINT projects_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX projects_ukey_name
  ON projects (name)
  WHERE (is_deleted IS FALSE);

CREATE TABLE apps
(
  id         SERIAL                 NOT NULL,
  project_id INTEGER                NOT NULL,
  platform   CHARACTER VARYING(255) NOT NULL,
  app_id     CHARACTER VARYING(255) NOT NULL,
  name       CHARACTER VARYING(255) NOT NULL,
  icon_href  VARCHAR(255),
  is_deleted BOOLEAN                NOT NULL DEFAULT FALSE,
  CONSTRAINT apps_pkey PRIMARY KEY (id),
  CONSTRAINT apps_fkey_project FOREIGN KEY (project_id) REFERENCES projects (id) MATCH SIMPLE
);
CREATE UNIQUE INDEX apps_ukey_platform_and_identifier
  ON apps (platform, app_id)
  WHERE (is_deleted IS FALSE);

CREATE TABLE builds
(
  id                 SERIAL       NOT NULL,
  app_id             INTEGER      NOT NULL,
  build_identifier   VARCHAR(255) NOT NULL,
  version_number     VARCHAR(255),
  release_notes      TEXT,
  bytes              BIGINT,
  href               VARCHAR(255) NOT NULL,
  releaser_id        INTEGER      NOT NULL,
  creation_timestamp BIGINT       NOT NULL,
  is_published       BOOLEAN      NOT NULL,
  is_deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
  CONSTRAINT builds_pkey PRIMARY KEY (id),
  CONSTRAINT builds_fkey_app FOREIGN KEY (app_id) REFERENCES apps (id) MATCH SIMPLE,
  CONSTRAINT builds_fkey_releaser FOREIGN KEY (releaser_id) REFERENCES store_users (id) MATCH SIMPLE
);
CREATE UNIQUE INDEX builds_ukey_app_and_identifier
  ON builds (app_id, build_identifier)
  WHERE (is_deleted IS FALSE);

CREATE TABLE verification_codes
(
  id            SERIAL       NOT NULL,
  device_id     VARCHAR(255) NOT NULL,
  encoded_value VARCHAR(255) NOT NULL,
  CONSTRAINT verification_codes_pkey PRIMARY KEY (id),
  CONSTRAINT verification_codes_ukey_device_and_value UNIQUE (device_id, encoded_value)
);

CREATE TABLE global_permissions
(
  id      SERIAL                 NOT NULL,
  type    CHARACTER VARYING(255) NOT NULL,
  user_id INTEGER                NOT NULL,
  CONSTRAINT global_permissions_pkey PRIMARY KEY (id),
  CONSTRAINT global_permissions_fkey_user FOREIGN KEY (user_id) REFERENCES store_users (id) MATCH SIMPLE
);

CREATE TABLE project_permissions
(
  id         SERIAL                 NOT NULL,
  type       CHARACTER VARYING(255) NOT NULL,
  project_id INTEGER                NOT NULL,
  user_id    INTEGER                NOT NULL,
  CONSTRAINT project_permissions_pkey PRIMARY KEY (id),
  CONSTRAINT project_permissions_fkey_project FOREIGN KEY (project_id) REFERENCES projects (id) MATCH SIMPLE,
  CONSTRAINT project_permissions_fkey_user FOREIGN KEY (user_id) REFERENCES store_users (id) MATCH SIMPLE
);

CREATE TABLE users_assigned_to_projects
(
  assignee_id INTEGER NOT NULL,
  project_id  INTEGER NOT NULL,
  CONSTRAINT user_project_pkey PRIMARY KEY (assignee_id, project_id),
  CONSTRAINT user_project_fkey_project FOREIGN KEY (project_id) REFERENCES projects (id) MATCH SIMPLE,
  CONSTRAINT user_project_fkey_assignee FOREIGN KEY (assignee_id) REFERENCES store_users (id) MATCH SIMPLE
);

CREATE TABLE projects_archived_by_users
(
  user_id             INTEGER NOT NULL,
  archived_project_id INTEGER NOT NULL,
  CONSTRAINT user_archived_project_pkey PRIMARY KEY (user_id, archived_project_id),
  CONSTRAINT user_archived_project_fkey_project FOREIGN KEY (archived_project_id) REFERENCES projects (id) MATCH SIMPLE,
  CONSTRAINT user_archived_project_fkey_user FOREIGN KEY (user_id) REFERENCES store_users (id) MATCH SIMPLE
);

CREATE TABLE project_members
(
  member_id  INTEGER NOT NULL,
  project_id INTEGER NOT NULL,
  CONSTRAINT user_member_project_pkey PRIMARY KEY (member_id, project_id),
  CONSTRAINT user_member_project_fkey_project FOREIGN KEY (project_id) REFERENCES projects (id) MATCH SIMPLE,
  CONSTRAINT user_member_project_fkey_user FOREIGN KEY (member_id) REFERENCES store_users (id) MATCH SIMPLE
);

CREATE TABLE apps_muted_by_users
(
  user_id      INTEGER NOT NULL,
  muted_app_id INTEGER NOT NULL,
  CONSTRAINT user_muted_app_pkey PRIMARY KEY (user_id, muted_app_id),
  CONSTRAINT user_muted_app_fkey_app FOREIGN KEY (muted_app_id) REFERENCES apps (id) MATCH SIMPLE,
  CONSTRAINT user_muted_app_fkey_user FOREIGN KEY (user_id) REFERENCES store_users (id) MATCH SIMPLE
);

CREATE TABLE builds_favorited_by_users
(
  user_id           INTEGER NOT NULL,
  favorite_build_id INTEGER NOT NULL,
  CONSTRAINT user_favorite_build_pkey PRIMARY KEY (user_id, favorite_build_id),
  CONSTRAINT user_favorite_build_fkey_build FOREIGN KEY (favorite_build_id) REFERENCES builds (id) MATCH SIMPLE,
  CONSTRAINT user_favorite_build_fkey_user FOREIGN KEY (user_id) REFERENCES store_users (id) MATCH SIMPLE
);
