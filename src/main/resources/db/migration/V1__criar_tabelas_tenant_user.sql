CREATE TABLE tenants (
                         id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         name          VARCHAR(100)  NOT NULL,
                         email         VARCHAR(150)  NOT NULL UNIQUE,
                         plan          VARCHAR(20)   NOT NULL DEFAULT 'TRIAL',
                         plan_status   VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
                         whatsapp      VARCHAR(20),
                         trial_ends_at TIMESTAMP     NOT NULL,
                         active        BOOLEAN       NOT NULL DEFAULT TRUE,
                         created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
                         updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
                       id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       tenant_id     UUID          NOT NULL REFERENCES tenants(id),
                       email         VARCHAR(150)  NOT NULL UNIQUE,
                       password_hash VARCHAR(255)  NOT NULL,
                       name          VARCHAR(100)  NOT NULL,
                       role          VARCHAR(20)   NOT NULL DEFAULT 'USER',
                       active        BOOLEAN       NOT NULL DEFAULT TRUE,
                       created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
                       updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_email     ON users(email);