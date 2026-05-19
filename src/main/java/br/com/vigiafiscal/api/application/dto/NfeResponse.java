package br.com.vigiafiscal.api.application.dto;

import br.com.vigiafiscal.api.domain.nfe.NfeStatus;
import br.com.vigiafiscal.api.domain.nfe.NotaFiscal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record NfeResponse(
        UUID id,
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
        String destinatarioNome,
        String ultimoEvento,
        UUID cnpjId,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        String statusManifestacao  // "FINAL", "CIENCIA" ou null
) {
    public static NfeResponse from(NotaFiscal nfe) {
        return from(nfe, null);
    }

    public static NfeResponse from(NotaFiscal nfe, String statusManifestacao) {
        return new NfeResponse(
                nfe.getId(),
                nfe.getChaveAcesso(),
                nfe.getNumero(),
                nfe.getSerie(),
                nfe.getDataEmissao(),
                nfe.getDataAutorizacao(),
                nfe.getValorTotal(),
                nfe.getStatus(),
                nfe.getEmitenteCnpj(),
                nfe.getEmitenteNome(),
                nfe.getDestinatarioCnpj(),
                nfe.getDestinatarioNome(),
                nfe.getUltimoEvento(),
                nfe.getCnpj().getId(),
                nfe.getCriadoEm(),
                nfe.getAtualizadoEm(),
                statusManifestacao
        );
    }
}
