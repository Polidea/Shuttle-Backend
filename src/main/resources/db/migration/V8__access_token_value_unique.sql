ALTER TABLE access_tokens
  ADD CONSTRAINT access_tokens_ukey_value UNIQUE (value);
