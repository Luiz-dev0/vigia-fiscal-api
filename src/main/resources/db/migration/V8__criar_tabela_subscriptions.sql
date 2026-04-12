CREATE TABLE subscriptions (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               tenant_id UUID NOT NULL REFERENCES tenants(id),
                               plan VARCHAR(20) NOT NULL,
                               status VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
                               stripe_customer_id VARCHAR(100),
                               stripe_subscription_id VARCHAR(100),
                               stripe_price_id VARCHAR(100),
                               current_period_end TIMESTAMP,
                               amount NUMERIC(10, 2),
                               created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_tenant_id ON subscriptions(tenant_id);
CREATE INDEX idx_subscriptions_stripe_subscription_id ON subscriptions(stripe_subscription_id);