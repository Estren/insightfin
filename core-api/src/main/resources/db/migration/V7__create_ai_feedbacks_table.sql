CREATE TABLE ai_feedbacks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    reference_month VARCHAR(7),
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_ai_feedbacks_user ON ai_feedbacks(user_id, created_at DESC);
CREATE INDEX idx_ai_feedbacks_type ON ai_feedbacks(user_id, type);
