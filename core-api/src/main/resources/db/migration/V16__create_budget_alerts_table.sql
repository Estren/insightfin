CREATE TABLE budget_alerts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    budget_id UUID NOT NULL REFERENCES budgets(id) ON DELETE CASCADE,
    threshold_percentage INTEGER NOT NULL,
    amount_spent DECIMAL(12, 2) NOT NULL,
    budget_amount DECIMAL(12, 2) NOT NULL,
    triggered_at TIMESTAMP NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    UNIQUE (budget_id, threshold_percentage)
);

CREATE INDEX idx_budget_alerts_user_read ON budget_alerts(user_id, read);
CREATE INDEX idx_budget_alerts_user_triggered ON budget_alerts(user_id, triggered_at DESC);
