CREATE TABLE recurring_transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id),
    type VARCHAR(10) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    description VARCHAR(255),
    frequency VARCHAR(10) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    next_occurrence DATE NOT NULL,
    last_generated_at DATE,
    paused BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_recurring_transactions_user ON recurring_transactions(user_id);
CREATE INDEX idx_recurring_transactions_next_occurrence ON recurring_transactions(next_occurrence) WHERE paused = FALSE;
CREATE INDEX idx_recurring_transactions_category ON recurring_transactions(category_id);

ALTER TABLE transactions
    ADD COLUMN recurring_transaction_id UUID REFERENCES recurring_transactions(id) ON DELETE SET NULL;

CREATE INDEX idx_transactions_recurring ON transactions(recurring_transaction_id);
