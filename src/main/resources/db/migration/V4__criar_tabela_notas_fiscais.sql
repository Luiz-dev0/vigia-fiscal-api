CREATE TABLE notas_fiscais (
                               id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               tenant_id        UUID NOT NULL REFERENCES tenants(id),
                               cnpj_id          UUID NOT NULL REFERENCES cnpjs(id),
                               chave_acesso     VARCHAR(44) NOT NULL,
                               numero           VARCHAR(20),
                               serie            VARCHAR(3),
                               data_emissao     TIMESTAMP,
                               data_autorizacao TIMESTAMP,
                               valor_total      NUMERIC(15, 2),
                               status           VARCHAR(30) NOT NULL DEFAULT 'PENDENTE',
                               emitente_cnpj    VARCHAR(14),
                               emitente_nome    VARCHAR(255),
                               destinatario_cnpj VARCHAR(14),
                               destinatario_nome VARCHAR(255),
                               xml_url          TEXT,
                               danfe_url        TEXT,
                               ultimo_evento    VARCHAR(100),
                               criado_em        TIMESTAMP NOT NULL DEFAULT NOW(),
                               atualizado_em    TIMESTAMP NOT NULL DEFAULT NOW(),
                               CONSTRAINT uq_chave_acesso UNIQUE (chave_acesso)
);

CREATE INDEX idx_notas_fiscais_tenant_id ON notas_fiscais(tenant_id);
CREATE INDEX idx_notas_fiscais_cnpj_id ON notas_fiscais(cnpj_id);
CREATE INDEX idx_notas_fiscais_status ON notas_fiscais(status);
CREATE INDEX idx_notas_fiscais_data_emissao ON notas_fiscais(data_emissao);