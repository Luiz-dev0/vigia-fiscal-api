package br.com.nfemonitor.api.infrastructure.sefaz;

import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;

@Component
public class NFeRecepcaoEventoClient {

    private static final String ENDPOINT =
            "https://www.nfe.fazenda.gov.br/NFeRecepcaoEvento4/NFeRecepcaoEvento4.asmx";
    private static final String SOAP_ACTION =
            "http://www.portalfiscal.inf.br/nfe/wsdl/NFeRecepcaoEvento4/nfeRecepcaoEvento";

    /**
     * Envia o XML de evento assinado via SOAP 1.2 para a SEFAZ e retorna o XML de resposta.
     */
    public String enviar(String xmlAssinado, KeyStore keyStore, String keyStorePassword) {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyStorePassword.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            HttpClient client = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();

            String soapEnvelope = buildSoapEnvelope(xmlAssinado);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Content-Type", "application/soap+xml; charset=utf-8")
                    .header("SOAPAction", SOAP_ACTION)
                    .POST(HttpRequest.BodyPublishers.ofString(soapEnvelope))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("SEFAZ retornou HTTP " + response.statusCode()
                        + ": " + response.body());
            }

            return response.body();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com SEFAZ: " + e.getMessage(), e);
        }
    }

    private String buildSoapEnvelope(String xmlAssinado) {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <soap12:Envelope
                    xmlns:soap12="http://www.w3.org/2003/05/soap-envelope"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xmlns:xsd="http://www.w3.org/2001/XMLSchema">
                  <soap12:Header>
                    <nfeCabecMsg xmlns="http://www.portalfiscal.inf.br/nfe/wsdl/NFeRecepcaoEvento4">
                      <cUF>31</cUF>
                      <versaoDados>1.00</versaoDados>
                    </nfeCabecMsg>
                  </soap12:Header>
                  <soap12:Body>
                    <nfeRecepcaoEvento xmlns="http://www.portalfiscal.inf.br/nfe/wsdl/NFeRecepcaoEvento4">
                      <nfeDadosMsg>
                """ + xmlAssinado + """
                      </nfeDadosMsg>
                    </nfeRecepcaoEvento>
                  </soap12:Body>
                </soap12:Envelope>
                """;
    }
}