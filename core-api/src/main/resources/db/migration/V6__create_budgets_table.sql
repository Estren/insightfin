CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    category_id UUID NOT NULL REFERENCES categories(id),
    amount DECIMAL(12, 2) NOT NULL,
    month VARCHAR(7) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(user_id, category_id, month)
);

CREATE INDEX idx_budgets_user_month ON budgets(user_id, month);
