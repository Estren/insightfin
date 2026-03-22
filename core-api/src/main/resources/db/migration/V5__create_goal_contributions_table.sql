CREATE TABLE goal_contributions (
    id UUID PRIMARY KEY,
    goal_id UUID NOT NULL REFERENCES goals(id),
    amount DECIMAL(12, 2) NOT NULL,
    date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_goal_contributions_goal_date ON goal_contributions(goal_id, date);
