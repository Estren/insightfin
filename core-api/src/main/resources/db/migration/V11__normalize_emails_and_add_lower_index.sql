-- Normalize all existing emails to lowercase + trimmed.
-- Prerequisite: no case-insensitive duplicates exist (verified on 2026-05-15).
UPDATE users
SET email = lower(trim(email))
WHERE email <> lower(trim(email));

-- Replace the case-sensitive UNIQUE constraint with a case-insensitive UNIQUE INDEX.
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;
CREATE UNIQUE INDEX IF NOT EXISTS users_email_lower_idx ON users (lower(email));
