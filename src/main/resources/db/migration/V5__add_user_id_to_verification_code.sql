ALTER TABLE verification_codes ADD COLUMN user_id INTEGER;

ALTER TABLE verification_codes
  ADD CONSTRAINT verification_codes_fkey_user_id FOREIGN KEY (user_id) REFERENCES store_users (id) MATCH SIMPLE;
