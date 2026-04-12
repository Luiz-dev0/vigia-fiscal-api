package br.com.nfemonitor.api.infrastructure.sefaz;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("src/test")
public class SefazClientFake implements SefazClient {

    @Override
    public List<NfeDados> consultarNfes(String cnpj) {
        return List.of(); // retorna lista vazia — sem chamadas externas
    }
}