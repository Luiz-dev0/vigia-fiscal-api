CREATE INDEX IF NOT EXISTS idx_nfe_tenant_data_emissao
    ON notas_fiscais (tenant_id, data_emissao);