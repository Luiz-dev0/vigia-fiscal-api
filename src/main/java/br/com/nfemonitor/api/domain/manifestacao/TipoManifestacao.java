package br.com.nfemonitor.api.domain.manifestacao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoManifestacao {

    CIENCIA_OPERACAO("210210", "Ciência da Operação"),
    CONFIRMACAO_OPERACAO("210200", "Confirmação da Operação"),
    DESCONHECIMENTO_OPERACAO("210220", "Desconhecimento da Operação"),
    OPERACAO_NAO_REALIZADA("210240", "Operação não Realizada");

    private final String codigo;
    private final String descricao;
}