package br.com.vigiafiscal.api.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManifestacaoRequest {
    private String tipoEvento;
    private String senha;
}
