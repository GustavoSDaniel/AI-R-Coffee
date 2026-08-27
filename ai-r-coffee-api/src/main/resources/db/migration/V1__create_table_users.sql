CREATE TABLE users (
                       id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                       keycloak_id  VARCHAR(255)  NOT NULL UNIQUE,
                       email        VARCHAR(255)  NOT NULL UNIQUE,
                       user_name    VARCHAR(255)  NOT NULL,
                       role         VARCHAR(50)   NOT NULL DEFAULT 'CONSUMER',
                       active       BOOLEAN       NOT NULL DEFAULT TRUE,
                       created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                       updated_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_user_name ON users(user_name);
CREATE INDEX idx_users_email_lower ON users(LOWER(email));