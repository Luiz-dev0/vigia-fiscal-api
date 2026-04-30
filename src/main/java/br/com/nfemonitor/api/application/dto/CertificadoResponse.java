package br.com.nfemonitor.api.application.dto;

import br.com.nfemonitor.api.domain.certificado.CertificadoA1;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CertificadoResponse {

    private UUID cnpjId;
    private LocalDateTime validade;
    private boolean vencido;

    public static CertificadoResponse from(CertificadoA1 certificado) {
        return CertificadoResponse.builder()
                .cnpjId(certificado.getCnpj().getId())
                .validade(certificado.getValidade())
                .vencido(certificado.getValidade().isBefore(LocalDateTime.now()))
                .build();
    }
}