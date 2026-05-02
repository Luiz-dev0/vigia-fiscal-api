package br.com.nfemonitor.api.domain.manifestacao;

import br.com.nfemonitor.api.domain.certificado.CertificadoService;
import br.com.nfemonitor.api.domain.nfe.NotaFiscal;
import br.com.nfemonitor.api.domain.nfe.NfeRepository;
import br.com.nfemonitor.api.infrastructure.sefaz.NFeRecepcaoEventoClient;
import br.com.nfemonitor.api.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManifestacaoService {

    private final ManifestacaoRepository manifestacaoRepository;
    private final NfeRepository nfeRepository;
    private final CertificadoService certificadoService;
    private final NFeRecepcaoEventoClient recepcaoEventoClient;

    @Transactional
    public ManifestacaoDestinatario manifestar(UUID nfeId, TipoManifestacao tipo, String senha) {
        UUID tenantId = TenantContext.get();

        NotaFiscal nfe = nfeRepository.findByIdAndTenantId(nfeId, tenantId)
                .orElseThrow(() -> new RuntimeException("NF-e não encontrada"));

        KeyStore keyStore = certificadoService.carregar(nfe.getCnpj().getId(), senha);

        String xmlEvento = montarXmlEvento(nfe, tipo);
        String xmlAssinado = assinarXml(xmlEvento, keyStore, senha);
        String retornoSefaz = recepcaoEventoClient.enviar(xmlAssinado, keyStore, senha);

        String cStat = extrairTag(retornoSefaz, "cStat");
        String xMotivo = extrairTag(retornoSefaz, "xMotivo");
        String protocolo = extrairTag(retornoSefaz, "nProt");
        String status = ("135".equals(cStat) || "136".equals(cStat)) ? "AUTORIZADO" : "REJEITADO";

        ManifestacaoDestinatario manifestacao = ManifestacaoDestinatario.builder()
                .tenant(nfe.getTenant())
                .notaFiscal(nfe)
                .cnpj(nfe.getCnpj())
                .tipoEvento(tipo)
                .codigoEvento(tipo.getCodigo())
                .protocolo(protocolo)
                .status(status)
                .cStat(cStat)
                .xMotivo(xMotivo)
                .enviadoEm(LocalDateTime.now())
                .build();

        return manifestacaoRepository.save(manifestacao);
    }

    @Transactional(readOnly = true)
    public List<ManifestacaoDestinatario> listarPorNfe(UUID nfeId) {
        UUID tenantId = TenantContext.get();
        return manifestacaoRepository.findAllByNotaFiscalIdAndTenantId(nfeId, tenantId);
    }

    /**
     * Chamado pelo job — envia Ciência da Operação automaticamente.
     * Nunca lança exceção para não derrubar o monitoramento.
     */
    public void cienciaAutomatica(NotaFiscal nfe, String senha) {
        try {
            if (senha == null || senha.isBlank()) {
                log.debug("Ciência automática ignorada para NF-e {}: senha não disponível", nfe.getId());
                return;
            }

            boolean jaEnviada = manifestacaoRepository
                    .existsByNotaFiscalIdAndTipoEvento(nfe.getId(), TipoManifestacao.CIENCIA_OPERACAO);

            if (jaEnviada) {
                log.debug("Ciência da Operação já enviada para NF-e {}", nfe.getId());
                return;
            }

            manifestar(nfe.getId(), TipoManifestacao.CIENCIA_OPERACAO, senha);
            log.info("Ciência da Operação enviada com sucesso para NF-e {}", nfe.getId());

        } catch (Exception e) {
            log.warn("Falha ao enviar Ciência da Operação para NF-e {}: {}", nfe.getId(), e.getMessage());
        }
    }

    // ─── XML ────────────────────────────────────────────────────────────────────

    private String montarXmlEvento(NotaFiscal nfe, TipoManifestacao tipo) {
        String dhEvento = ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
        String cOrgao = "91"; // RFB — manifestação do destinatário é nacional
        String cnpjDestinatario = nfe.getDestinatarioCnpj();
        String chaveAcesso = nfe.getChaveAcesso();
        String nSeqEvento = "1";

        return """
                <envEvento versao="1.00" xmlns="http://www.portalfiscal.inf.br/nfe">
                  <idLote>1</idLote>
                  <evento versao="1.00">
                    <infEvento Id="ID%s%s%s">
                      <cOrgao>%s</cOrgao>
                      <tpAmb>1</tpAmb>
                      <CNPJ>%s</CNPJ>
                      <chNFe>%s</chNFe>
                      <dhEvento>%s</dhEvento>
                      <tpEvento>%s</tpEvento>
                      <nSeqEvento>%s</nSeqEvento>
                      <verEvento>1.00</verEvento>
                      <detEvento versao="1.00">
                        <descEvento>%s</descEvento>
                      </detEvento>
                    </infEvento>
                  </evento>
                </envEvento>
                """.formatted(
                tipo.getCodigo(),
                chaveAcesso,
                String.format("%02d", Integer.parseInt(nSeqEvento)),
                cOrgao,
                cnpjDestinatario,
                chaveAcesso,
                dhEvento,
                tipo.getCodigo(),
                nSeqEvento,
                tipo.getDescricao()
        );
    }

    private String assinarXml(String xmlStr, KeyStore keyStore, String senha) {
        try {
            // Carrega chave privada e certificado
            String alias = keyStore.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, senha.toCharArray());
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);

            // Parseia o XML
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(xmlStr.getBytes()));

            // Encontra o elemento infEvento para referenciar na assinatura
            Element infEvento = (Element) doc.getElementsByTagNameNS("*", "infEvento").item(0);
            String id = infEvento.getAttribute("Id");

            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            // Transforms: enveloped + C14N
            List<Transform> transforms = List.of(
                    fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                    fac.newTransform("http://www.w3.org/TR/2001/REC-xml-c14n-20010315",
                            (TransformParameterSpec) null)
            );

            Reference ref = fac.newReference(
                    "#" + id,
                    fac.newDigestMethod(DigestMethod.SHA1, null),
                    transforms,
                    null,
                    null
            );

            SignedInfo si = fac.newSignedInfo(
                    fac.newCanonicalizationMethod(
                            CanonicalizationMethod.INCLUSIVE,
                            (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                    Collections.singletonList(ref)
            );

            KeyInfoFactory kif = fac.getKeyInfoFactory();
            X509Data x509Data = kif.newX509Data(Collections.singletonList(cert));
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509Data));

            XMLSignature signature = fac.newXMLSignature(si, ki);

            // Insere assinatura dentro do elemento <evento>
            Element evento = (Element) doc.getElementsByTagNameNS("*", "evento").item(0);
            DOMSignContext dsc = new DOMSignContext(privateKey, evento);
            signature.sign(dsc);

            // Serializa de volta para String
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter sw = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(sw));

            return sw.toString();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao assinar XML de manifestação: " + e.getMessage(), e);
        }
    }

    private String extrairTag(String xml, String tag) {
        Pattern pattern = Pattern.compile("<" + tag + ">([^<]*)</" + tag + ">");
        Matcher matcher = pattern.matcher(xml);
        return matcher.find() ? matcher.group(1) : null;
    }
}