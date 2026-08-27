CREATE TABLE categories (
                            id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                            name        VARCHAR(255)  NOT NULL UNIQUE,
                            description TEXT          NOT NULL,
                            active      BOOLEAN       NOT NULL DEFAULT TRUE,
                            created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                            updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()

);

CREATE INDEX idx_categories_name_lower ON categories(LOWER(name));
CREATE INDEX idx_categories_active ON categories(active) WHERE active = TRUE;