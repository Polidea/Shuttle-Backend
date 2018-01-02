-- We want to connect optional Device ID with Access Token and its Owner.
-- In effect every time request is performed we can obtain ID of Device (if any) from which it was performed.
ALTER TABLE tokens
  ADD COLUMN device_id VARCHAR(255);

-- All existing Push Tokens have no Device ID stored.
-- But we can delete them all because they will be registered again with next login from mobile device.
DELETE FROM push_tokens;
-- And now we can add non-null Device ID column.
ALTER TABLE push_tokens
  ADD COLUMN device_id VARCHAR(255) NOT NULL
  DEFAULT 'If this Device ID value exists, then we failed in deleting all Push Tokens before adding non-null Device ID column... :-(';
-- And we can remove old, unnecessary constraints.
ALTER TABLE push_tokens
  DROP CONSTRAINT push_tokens_ukey_owner_and_platform_and_value;
-- And we can assume that there is only one Push Token per Device
ALTER TABLE push_tokens
  ADD CONSTRAINT push_tokens_ukey_device_id UNIQUE (device_id);
