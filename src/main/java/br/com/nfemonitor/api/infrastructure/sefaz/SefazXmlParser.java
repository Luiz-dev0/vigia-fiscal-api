package br.com.nfemonitor.api.infrastructure.sefaz;

import br.com.nfemonitor.api.domain.nfe.NfeStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class SefazXmlParser {

    private static final DateTimeFormatter SEFAZ_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public NfeDados parseXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();

            String chave     = getTagValue(doc, "chNFe");
            String numero    = getTagValue(doc, "nNF");
            String serie     = getTagValue(doc, "serie");
            String dataEmStr = getTagValue(doc, "dhEmi");
            String valorStr  = getTagValue(doc, "vNF");
            String cStat     = getTagValue(doc, "cStat");

            String emitCnpj  = getTagValueFromParent(doc, "emit", "CNPJ");
            String emitNome  = getTagValueFromParent(doc, "emit", "xNome");
            String destCnpj  = getTagValueFromParent(doc, "dest", "CNPJ");
            String destNome  = getTagValueFromParent(doc, "dest", "xNome");

            LocalDateTime dataEmissao = dataEmStr != null
                    ? LocalDateTime.parse(dataEmStr, SEFAZ_DATE_FORMAT)
                    : null;

            BigDecimal valor = valorStr != null
                    ? new BigDecimal(valorStr)
                    : BigDecimal.ZERO;

            NfeStatus status = resolverStatus(cStat);

            return new NfeDados(chave, numero, serie, dataEmissao,
                    null, valor, status, emitCnpj, emitNome, destCnpj, destNome);

        } catch (Exception e) {
            log.error("Erro ao parsear XML da NF-e: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao processar XML da NF-e", e);
        }
    }

    private String getTagValue(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagNameNS("*", tagName);
        if (nodes.getLength() == 0) {
            nodes = doc.getElementsByTagName(tagName);
        }
        if (nodes.getLength() > 0 && nodes.item(0).getFirstChild() != null) {
            return nodes.item(0).getFirstChild().getNodeValue();
        }
        return null;
    }

    private String getTagValueFromParent(Document doc, String parentTag, String childTag) {
        NodeList parents = doc.getElementsByTagNameNS("*", parentTag);
        if (parents.getLength() == 0) {
            parents = doc.getElementsByTagName(parentTag);
        }
        if (parents.getLength() > 0) {
            NodeList children = ((org.w3c.dom.Element) parents.item(0))
                    .getElementsByTagName(childTag);
            if (children.getLength() > 0 && children.item(0).getFirstChild() != null) {
                return children.item(0).getFirstChild().getNodeValue();
            }
        }
        return null;
    }

    private NfeStatus resolverStatus(String cStat) {
        if (cStat == null) return NfeStatus.PENDENTE;
        return switch (cStat) {
            case "100" -> NfeStatus.AUTORIZADA;
            case "101", "102" -> NfeStatus.CANCELADA;
            case "110", "301", "302" -> NfeStatus.DENEGADA;
            default -> NfeStatus.PENDENTE;
        };
    }
}