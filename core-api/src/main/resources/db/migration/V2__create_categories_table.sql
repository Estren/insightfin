CREATE TABLE categories (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(50) NOT NULL,
    type VARCHAR(10) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(7),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_categories_user_type ON categories(user_id, type);
