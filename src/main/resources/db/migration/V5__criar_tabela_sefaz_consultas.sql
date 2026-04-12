CREATE TABLE sefaz_consultas (
                                 id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 tenant_id         UUID NOT NULL REFERENCES tenants(id),
                                 cnpj_id           UUID NOT NULL REFERENCES cnpjs(id),
                                 tipo              VARCHAR(50) NOT NULL,
                                 status_http       INTEGER,
                                 sucesso           BOOLEAN NOT NULL DEFAULT FALSE,
                                 mensagem          TEXT,
                                 notas_encontradas INTEGER DEFAULT 0,
                                 iniciado_em       TIMESTAMP NOT NULL DEFAULT NOW(),
                                 finalizado_em     TIMESTAMP
);

CREATE INDEX idx_sefaz_consultas_tenant_id ON sefaz_consultas(tenant_id);
CREATE INDEX idx_sefaz_consultas_cnpj_id ON sefaz_consultas(cnpj_id);