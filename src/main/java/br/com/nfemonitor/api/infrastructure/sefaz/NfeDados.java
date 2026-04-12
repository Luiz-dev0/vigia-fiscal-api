package br.com.nfemonitor.api.infrastructure.sefaz;

import br.com.nfemonitor.api.domain.nfe.NfeStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NfeDados(
        String chaveAcesso,
        String numero,
        String serie,
        LocalDateTime dataEmissao,
        LocalDateTime dataAutorizacao,
        BigDecimal valorTotal,
        NfeStatus status,
        String emitenteCnpj,
        String emitenteNome,
        String destinatarioCnpj,
        String destinatarioNome
) {}