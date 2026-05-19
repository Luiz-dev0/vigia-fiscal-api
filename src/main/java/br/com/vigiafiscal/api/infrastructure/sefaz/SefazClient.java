package br.com.vigiafiscal.api.infrastructure.sefaz;

import java.util.List;

public interface SefazClient {
    List<NfeDados> consultarNfes(String cnpj);
}
