package br.com.nfemonitor.api.infrastructure.sefaz;

import java.util.List;

public interface SefazClient {
    List<NfeDados> consultarNfes(String cnpj);
}