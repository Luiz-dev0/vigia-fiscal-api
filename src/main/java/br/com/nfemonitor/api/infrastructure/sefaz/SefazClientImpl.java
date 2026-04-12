package br.com.nfemonitor.api.infrastructure.sefaz;

import br.com.nfemonitor.api.domain.nfe.NfeStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SefazClientImpl implements SefazClient {

    private static final String SOAP_ACTION =
            "http://www.portalfiscal.inf.br/nfe/wsdl/NFeDistribuicaoDFe/nfeDistDFeInteresse";

    private static final DateTimeFormatter SEFAZ_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Value("${sefaz.url.distribuicao:https://hom1.nfe.fazenda.gov.br/NFeDistribuicaoDFe/NFeDistribuicaoDFe.asmx}")
    private String sefazUrl;

    @Value("${sefaz.ambiente:2}")
    private String ambiente;

    @Value("${sefaz.cnpj.autorizador:}")
    private String cnpjAutorizador;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public List<NfeDados> consultarNfes(String cnpj) {
        log.info("[SefazClient] Consultando NF-es para CNPJ: {}", cnpj);

        try {
            String envelopeSOAP = montarEnvelopeSOAP(cnpj);
            log.debug("[SefazClient] Envelope SOAP montado:\n{}", envelopeSOAP);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sefazUrl))
                    .header("Content-Type", "application/soap+xml; charset=utf-8")
                    .header("SOAPAction", SOAP_ACTION)
                    .POST(HttpRequest.BodyPublishers.ofString(envelopeSOAP))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            log.info("[SefazClient] Status HTTP SEFAZ: {}", response.statusCode());
            log.debug("[SefazClient] Resposta SEFAZ:\n{}", response.body());

            if (response.statusCode() != 200) {
                throw new SefazException("SEFAZ retornou HTTP " + response.statusCode()
                        + ": " + response.body());
            }

            return parsearResposta(response.body(), cnpj);

        } catch (SefazException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SefazClient] Erro ao consultar SEFAZ para CNPJ {}: {}", cnpj, e.getMessage(), e);
            throw new SefazException("Falha na comunicação com SEFAZ: " + e.getMessage(), e);
        }
    }

    private String montarEnvelopeSOAP(String cnpj) throws Exception {
        // cUF 91 = Sefaz Nacional (ambiente nacional — aceito pela SEFAZ AN)
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap12:Envelope
                    xmlns:soap12="http://www.w3.org/2003/05/soap-envelope"
                    xmlns:nfe="http://www.portalfiscal.inf.br/nfe/wsdl/NFeDistribuicaoDFe">
                  <soap12:Header/>
                  <soap12:Body>
                    <nfe:nfeDistDFeInteresse>
                      <nfe:nfeDadosMsg>
                        <distDFeInt xmlns="http://www.portalfiscal.inf.br/nfe"
                                    versao="1.01">
                          <tpAmb>%s</tpAmb>
                          <cUFAutor>91</cUFAutor>
                          <CNPJ>%s</CNPJ>
                          <distNSU>
                            <ultNSU>000000000000000</ultNSU>
                          </distNSU>
                        </distDFeInt>
                      </nfe:nfeDadosMsg>
                    </nfe:nfeDistDFeInteresse>
                  </soap12:Body>
                </soap12:Envelope>
                """.formatted(ambiente, cnpj);
    }

    private List<NfeDados> parsearResposta(String xmlResposta, String cnpjConsultado) {
        List<NfeDados> resultado = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(
                    new java.io.ByteArrayInputStream(xmlResposta.getBytes()));

            // verifica cStat — 137 = nenhum documento, 138 = documentos encontrados
            NodeList cStatNodes = doc.getElementsByTagNameNS("*", "cStat");
            if (cStatNodes.getLength() > 0) {
                String cStat = cStatNodes.item(0).getTextContent().trim();
                log.info("[SefazClient] cStat SEFAZ: {}", cStat);

                if ("137".equals(cStat)) {
                    log.info("[SefazClient] Nenhum documento novo para CNPJ: {}", cnpjConsultado);
                    return resultado;
                }

                if (!"138".equals(cStat)) {
                    NodeList xMotivoNodes = doc.getElementsByTagNameNS("*", "xMotivo");
                    String motivo = xMotivoNodes.getLength() > 0
                            ? xMotivoNodes.item(0).getTextContent() : "desconhecido";
                    log.warn("[SefazClient] SEFAZ retornou cStat {} — {}", cStat, motivo);
                    return resultado;
                }
            }

            // processa cada docZip retornado
            NodeList docZips = doc.getElementsByTagNameNS("*", "docZip");
            log.info("[SefazClient] {} documento(s) encontrado(s) para CNPJ: {}",
                    docZips.getLength(), cnpjConsultado);

            for (int i = 0; i < docZips.getLength(); i++) {
                Element docZip = (Element) docZips.item(i);
                String schema = docZip.getAttribute("schema");

                // processa apenas NF-e (schema resNFe) e eventos (schema procEventoNFe)
                if (schema.startsWith("resNFe")) {
                    NfeDados nfe = parsearResNfe(docZip, cnpjConsultado);
                    if (nfe != null) resultado.add(nfe);
                } else if (schema.startsWith("procEventoNFe")) {
                    NfeDados nfe = parsearEvento(docZip, cnpjConsultado);
                    if (nfe != null) resultado.add(nfe);
                } else {
                    log.debug("[SefazClient] Schema ignorado: {}", schema);
                }
            }

        } catch (Exception e) {
            log.error("[SefazClient] Erro ao parsear resposta SEFAZ: {}", e.getMessage(), e);
            throw new SefazException("Erro ao processar resposta da SEFAZ: " + e.getMessage(), e);
        }

        return resultado;
    }

    private NfeDados parsearResNfe(Element docZip, String cnpjConsultado) {
        try {
            // o conteúdo do docZip é base64 + gzip — decodifica
            String conteudoBase64 = docZip.getTextContent().trim();
            byte[] gzipBytes = java.util.Base64.getDecoder().decode(conteudoBase64);
            String xmlNfe = descomprimirGzip(gzipBytes);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(xmlNfe.getBytes()));

            String chaveAcesso = getTagValue(doc, "chNFe");
            String numero      = getTagValue(doc, "nNF");
            String serie       = getTagValue(doc, "serie");
            String dhEmi       = getTagValue(doc, "dhEmi");
            String dhRecbto    = getTagValue(doc, "dhRecbto");
            String vNF         = getTagValue(doc, "vNF");
            String cSitNFe     = getTagValue(doc, "cSitNFe");
            String emitCnpj    = getTagValue(doc, "CNPJ");
            String emitNome    = getTagValue(doc, "xNome");

            NfeStatus status = mapearStatusNfe(cSitNFe);

            return new NfeDados(
                    chaveAcesso,
                    numero,
                    serie,
                    parsearData(dhEmi),
                    parsearData(dhRecbto),
                    new BigDecimal(vNF.isEmpty() ? "0" : vNF),
                    status,
                    emitCnpj,
                    emitNome,
                    cnpjConsultado,
                    ""
            );

        } catch (Exception e) {
            log.error("[SefazClient] Erro ao parsear resNFe: {}", e.getMessage(), e);
            return null;
        }
    }

    private NfeDados parsearEvento(Element docZip, String cnpjConsultado) {
        try {
            String conteudoBase64 = docZip.getTextContent().trim();
            byte[] gzipBytes = java.util.Base64.getDecoder().decode(conteudoBase64);
            String xmlEvento = descomprimirGzip(gzipBytes);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(xmlEvento.getBytes()));

            String chaveAcesso = getTagValue(doc, "chNFe");
            String tpEvento    = getTagValue(doc, "tpEvento");
            String dhEvento    = getTagValue(doc, "dhEvento");

            // 110111 = cancelamento
            if (!"110111".equals(tpEvento)) {
                log.debug("[SefazClient] Evento {} ignorado para chave {}", tpEvento, chaveAcesso);
                return null;
            }

            return new NfeDados(
                    chaveAcesso,
                    "",
                    "",
                    parsearData(dhEvento),
                    parsearData(dhEvento),
                    BigDecimal.ZERO,
                    NfeStatus.CANCELADA,
                    "",
                    "",
                    cnpjConsultado,
                    ""
            );

        } catch (Exception e) {
            log.error("[SefazClient] Erro ao parsear evento: {}", e.getMessage(), e);
            return null;
        }
    }

    private NfeStatus mapearStatusNfe(String cSitNFe) {
        return switch (cSitNFe) {
            case "1"  -> NfeStatus.AUTORIZADA;
            case "2"  -> NfeStatus.CANCELADA;
            case "3"  -> NfeStatus.DENEGADA;
            default   -> NfeStatus.REJEITADA;
        };
    }

    private String descomprimirGzip(byte[] gzipBytes) throws Exception {
        try (var gis = new java.util.zip.GZIPInputStream(
                new java.io.ByteArrayInputStream(gzipBytes));
             var reader = new java.io.InputStreamReader(gis, java.nio.charset.StandardCharsets.UTF_8);
             var buf = new java.io.BufferedReader(reader)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = buf.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private String getTagValue(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagNameNS("*", tagName);
        if (nodes.getLength() > 0) return nodes.item(0).getTextContent().trim();
        return "";
    }

    private LocalDateTime parsearData(String data) {
        if (data == null || data.isEmpty()) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(data, SEFAZ_DATE_FORMAT);
        } catch (Exception e) {
            log.warn("[SefazClient] Data inválida recebida da SEFAZ: {}", data);
            return LocalDateTime.now();
        }
    }
}