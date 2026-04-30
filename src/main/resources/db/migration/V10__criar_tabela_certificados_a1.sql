CREATE TABLE certificados_a1 (
                                 id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 tenant_id   UUID NOT NULL REFERENCES tenants(id),
                                 cnpj_id     UUID NOT NULL REFERENCES cnpjs(id),
                                 pfx_data    BYTEA NOT NULL,
                                 validade     TIMESTAMP NOT NULL,
                                 created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                                 updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                                 UNIQUE (cnpj_id)
);

CREATE INDEX idx_certificados_cnpj_id   ON certificados_a1(cnpj_id);
CREATE INDEX idx_certificados_tenant_id ON certificados_a1(tenant_id);