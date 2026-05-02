package br.com.nfemonitor.api.application.dto;

import br.com.nfemonitor.api.domain.manifestacao.ManifestacaoDestinatario;
import br.com.nfemonitor.api.domain.manifestacao.TipoManifestacao;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ManifestacaoResponse {

    private UUID id;
    private String tipoEvento;
    private String descricaoEvento;
    private String protocolo;
    private String status;
    private String cStat;
    private String xMotivo;
    private LocalDateTime enviadoEm;

    public static ManifestacaoResponse from(ManifestacaoDestinatario m) {
        return ManifestacaoResponse.builder()
                .id(m.getId())
                .tipoEvento(m.getTipoEvento().name())
                .descricaoEvento(m.getTipoEvento().getDescricao())
                .protocolo(m.getProtocolo())
                .status(m.getStatus())
                .cStat(m.getCStat())
                .xMotivo(m.getXMotivo())
                .enviadoEm(m.getEnviadoEm())
                .build();
    }
}