CREATE TABLE refresh_tokens
(
  id                 SERIAL                 NOT NULL,
  value              TEXT                   NOT NULL,
  owner_id           INTEGER                NOT NULL,
  device_id          VARCHAR(255)           NOT NULL,
  creation_timestamp TIMESTAMP              NOT NULL,
  CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id),
  CONSTRAINT refresh_tokens_fkey_owner FOREIGN KEY (owner_id) REFERENCES store_users (id) MATCH SIMPLE,
  CONSTRAINT refresh_tokens_ukey_owner_and_device UNIQUE (owner_id, device_id),
  CONSTRAINT refresh_tokens_ukey_value UNIQUE (value)
);
