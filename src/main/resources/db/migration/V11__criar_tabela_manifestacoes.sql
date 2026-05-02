CREATE TABLE manifestacoes (
                               id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               tenant_id        UUID         NOT NULL REFERENCES tenants(id),
                               nota_fiscal_id   UUID         NOT NULL REFERENCES notas_fiscais(id),
                               cnpj_id          UUID         NOT NULL REFERENCES cnpjs(id),
                               tipo_evento      VARCHAR(20)  NOT NULL,
                               codigo_evento    VARCHAR(6)   NOT NULL,
                               protocolo        VARCHAR(50),
                               status           VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',
                               c_stat           VARCHAR(10),
                               x_motivo         TEXT,
                               enviado_em       TIMESTAMP    NOT NULL DEFAULT NOW(),
                               created_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_manifestacoes_nota_fiscal_id ON manifestacoes(nota_fiscal_id);
CREATE INDEX idx_manifestacoes_tenant_id      ON manifestacoes(tenant_id);