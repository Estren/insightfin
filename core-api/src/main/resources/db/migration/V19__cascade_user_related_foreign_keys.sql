-- Make `DELETE /users/me` actually work.
--
-- Historically each table that points back at `users(id)` was created without
-- ON DELETE CASCADE, so deleting a user with any data caused a 500 with a
-- foreign-key violation (e.g. `categories_user_id_fkey`). Adding cascade here
-- finally makes account-deletion a single, safe operation and also unblocks
-- the seed script's `--reset` flow.
--
-- Two category-level FKs (transactions.category_id, budgets.category_id,
-- recurring_transactions.category_id, goal_contributions.goal_id) are cascaded
-- as well so the in-order delete works regardless of which parent row Postgres
-- processes first when several CASCADE paths converge.

ALTER TABLE categories
    DROP CONSTRAINT categories_user_id_fkey,
    ADD CONSTRAINT categories_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE transactions
    DROP CONSTRAINT transactions_user_id_fkey,
    ADD CONSTRAINT transactions_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE transactions
    DROP CONSTRAINT transactions_category_id_fkey,
    ADD CONSTRAINT transactions_category_id_fkey
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE;

ALTER TABLE goals
    DROP CONSTRAINT goals_user_id_fkey,
    ADD CONSTRAINT goals_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE goal_contributions
    DROP CONSTRAINT goal_contributions_goal_id_fkey,
    ADD CONSTRAINT goal_contributions_goal_id_fkey
        FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE;

ALTER TABLE budgets
    DROP CONSTRAINT budgets_user_id_fkey,
    ADD CONSTRAINT budgets_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE budgets
    DROP CONSTRAINT budgets_category_id_fkey,
    ADD CONSTRAINT budgets_category_id_fkey
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE;

ALTER TABLE ai_feedbacks
    DROP CONSTRAINT ai_feedbacks_user_id_fkey,
    ADD CONSTRAINT ai_feedbacks_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE budget_alerts
    DROP CONSTRAINT budget_alerts_user_id_fkey,
    ADD CONSTRAINT budget_alerts_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE recurring_transactions
    DROP CONSTRAINT recurring_transactions_category_id_fkey,
    ADD CONSTRAINT recurring_transactions_category_id_fkey
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE;

-- refresh_tokens.user_id is intentionally left without cascade — UserService.delete()
-- explicitly revokes refresh tokens before the user row is deleted, so the cascade
-- would be redundant; keeping the FK strict makes any future code path that forgets
-- to revoke surface as a hard error instead of silently dropping audit data.
