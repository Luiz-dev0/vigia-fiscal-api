package br.com.nfemonitor.api.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManifestacaoRequest {
    private String tipoEvento;
    private String senha;
}