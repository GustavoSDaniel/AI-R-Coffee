CREATE TABLE users (
                       id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                       keycloak_id VARCHAR(255)  NOT NULL UNIQUE,
                       user_name   VARCHAR(255)  NOT NULL UNIQUE,
                       role        VARCHAR(50)   NOT NULL DEFAULT 'EMPLOYEE',
                       is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
                       created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_users_user_name   ON users(user_name);