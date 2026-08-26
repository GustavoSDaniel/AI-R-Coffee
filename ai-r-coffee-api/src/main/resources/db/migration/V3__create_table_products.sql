CREATE TABLE products (
                          id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                          name        VARCHAR(255)  NOT NULL,
                          description TEXT,
                          price       DECIMAL(10, 2) NOT NULL,
                          image_url   VARCHAR(255),
                          category_id UUID          NOT NULL REFERENCES categories(id),
                          active      BOOLEAN       NOT NULL DEFAULT TRUE,
                          created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                          updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                          created_by  UUID          REFERENCES users(id),
                          updated_by  UUID          REFERENCES users(id)
);

CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_active      ON products(active) WHERE active = TRUE;