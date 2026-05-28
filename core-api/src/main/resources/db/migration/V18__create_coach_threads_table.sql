CREATE TABLE coach_threads (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    foundry_thread_id VARCHAR(64) NOT NULL,
    title VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_message_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_coach_threads_user_last
    ON coach_threads (user_id, last_message_at DESC);
