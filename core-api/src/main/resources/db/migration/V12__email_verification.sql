-- Add email verification columns to users.
ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN email_verified_at TIMESTAMP;

-- Backfill existing users as verified to avoid locking out accounts created before this feature.
UPDATE users SET email_verified = TRUE, email_verified_at = created_at WHERE email_verified = FALSE;

-- Table to track verification tokens for both registration confirmation and email change requests.
CREATE TABLE email_verification_tokens (
    id           UUID PRIMARY KEY,
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_email VARCHAR(255) NOT NULL,
    token_hash   VARCHAR(255) NOT NULL UNIQUE,
    pin_hash     VARCHAR(255),
    pin_attempts INTEGER NOT NULL DEFAULT 0,
    purpose      VARCHAR(20) NOT NULL,
    expires_at   TIMESTAMP NOT NULL,
    used_at      TIMESTAMP,
    created_at   TIMESTAMP NOT NULL
);

CREATE INDEX idx_email_verif_tokens_user ON email_verification_tokens(user_id);
CREATE INDEX idx_email_verif_tokens_hash ON email_verification_tokens(token_hash);
CREATE INDEX idx_email_verif_tokens_user_purpose ON email_verification_tokens(user_id, purpose);
