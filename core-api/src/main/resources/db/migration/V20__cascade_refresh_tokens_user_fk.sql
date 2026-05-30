-- V19 left `refresh_tokens.user_id` without cascade on purpose, expecting
-- `UserService.delete()` to revoke tokens first. But "revoke" here is a soft
-- update (`revoked = true`), not a row delete — so deleting the user still
-- fails on the FK. The cleanest fix is to make refresh tokens cascade like
-- the rest of the user-owned tables; the explicit revoke remains harmless.

ALTER TABLE refresh_tokens
    DROP CONSTRAINT refresh_tokens_user_id_fkey,
    ADD CONSTRAINT refresh_tokens_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
