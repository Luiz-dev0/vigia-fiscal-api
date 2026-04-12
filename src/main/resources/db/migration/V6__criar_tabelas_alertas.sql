CREATE TABLE alert_rules (
                             id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             tenant_id   UUID        NOT NULL REFERENCES tenants(id),
                             cnpj_id     UUID                 REFERENCES cnpjs(id),
                             event_type  VARCHAR(50) NOT NULL,
                             channel     VARCHAR(20) NOT NULL,
                             destination VARCHAR(255) NOT NULL,
                             minutes_before INTEGER  NOT NULL DEFAULT 60,
                             active      BOOLEAN     NOT NULL DEFAULT TRUE,
                             created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
                             updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE alert_events (
                              id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              rule_id         UUID        NOT NULL REFERENCES alert_rules(id),
                              nota_fiscal_id  UUID        NOT NULL REFERENCES notas_fiscais(id),
                              status          VARCHAR(50) NOT NULL,
                              sent_at         TIMESTAMP,
                              error_msg       TEXT,
                              created_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_alert_rules_tenant ON alert_rules(tenant_id);
CREATE INDEX idx_alert_events_rule  ON alert_events(rule_id);