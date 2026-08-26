CREATE TABLE categories (
                            id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                            name        VARCHAR(255)  NOT NULL UNIQUE,
                            description TEXT,
                            parent_id   UUID          REFERENCES categories(id) ON DELETE SET NULL,
                            active      BOOLEAN       NOT NULL DEFAULT TRUE,
                            created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                            updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                            created_by  UUID,
                            updated_by  UUID
);

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_active    ON categories(active) WHERE active = TRUE;