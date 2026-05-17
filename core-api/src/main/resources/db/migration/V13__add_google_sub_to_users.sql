ALTER TABLE users ADD COLUMN google_sub VARCHAR(255);
CREATE UNIQUE INDEX uk_users_google_sub ON users (google_sub);

ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;
