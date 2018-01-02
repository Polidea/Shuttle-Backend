-- Let's rename `tokens` table to `access_tokens` so it will be more distinct from `push_tokens` table.
ALTER TABLE tokens
  RENAME TO access_tokens;
