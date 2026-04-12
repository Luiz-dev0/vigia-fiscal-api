CREATE TABLE cnpjs (
                       id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       tenant_id        UUID          NOT NULL REFERENCES tenants(id),
                       cnpj             VARCHAR(18)   NOT NULL,
                       razao_social     VARCHAR(200)  NOT NULL,
                       nome_fantasia    VARCHAR(200),
                       ie               VARCHAR(30),
                       uf               CHAR(2)       NOT NULL,
                       email_contato    VARCHAR(150),
                       active           BOOLEAN       NOT NULL DEFAULT TRUE,
                       last_consulted_at TIMESTAMP,
                       created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
                       updated_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
                       UNIQUE (tenant_id, cnpj)
);

CREATE INDEX idx_cnpjs_tenant_id ON cnpjs(tenant_id);
CREATE INDEX idx_cnpjs_cnpj      ON cnpjs(cnpj);