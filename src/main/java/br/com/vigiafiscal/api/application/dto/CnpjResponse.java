package br.com.vigiafiscal.api.application.dto;

import br.com.vigiafiscal.api.domain.cnpj.Cnpj;
import java.time.LocalDateTime;
import java.util.UUID;

public record CnpjResponse(
        UUID id,
        String cnpj,
        String razaoSocial,
        String nomeFantasia,
        String uf,
        Boolean active,
        LocalDateTime lastConsultedAt
) {
    public static CnpjResponse from(Cnpj c) {
        return new CnpjResponse(c.getId(), c.getCnpj(), c.getRazaoSocial(),
                c.getNomeFantasia(), c.getUf(), c.getActive(), c.getLastConsultedAt());
    }
}
