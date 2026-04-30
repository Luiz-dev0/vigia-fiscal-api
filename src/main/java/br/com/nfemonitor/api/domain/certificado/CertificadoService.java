package br.com.nfemonitor.api.domain.certificado;

import br.com.nfemonitor.api.domain.cnpj.Cnpj;
import br.com.nfemonitor.api.domain.cnpj.CnpjRepository;
import br.com.nfemonitor.api.domain.tenant.Tenant;
import br.com.nfemonitor.api.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificadoService {

    private final CertificadoRepository certificadoRepository;
    private final CnpjRepository cnpjRepository;

    @Transactional
    public CertificadoA1 salvar(UUID cnpjId, byte[] pfxBytes, String senha) {
        UUID tenantId = TenantContext.get();

        Cnpj cnpj = cnpjRepository.findByIdAndTenantId(cnpjId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("CNPJ não encontrado."));

        KeyStore keyStore = carregarKeyStore(pfxBytes, senha);
        LocalDateTime validade = extrairValidade(keyStore);

        if (validade.isBefore(LocalDateTime.now())) {
            throw new CertificadoException.CertificadoVencidoException();
        }

        CertificadoA1 certificado = certificadoRepository
                .findByCnpjIdAndTenantId(cnpjId, tenantId)
                .orElse(CertificadoA1.builder()
                        .tenant(Tenant.builder().id(tenantId).build())
                        .cnpj(cnpj)
                        .build());

        certificado.setPfxData(pfxBytes);
        certificado.setValidade(validade);

        return certificadoRepository.save(certificado);
    }

    @Transactional(readOnly = true)
    public KeyStore carregar(UUID cnpjId, String senha) {
        UUID tenantId = TenantContext.get();

        CertificadoA1 certificado = certificadoRepository
                .findByCnpjIdAndTenantId(cnpjId, tenantId)
                .orElseThrow(() -> new CertificadoException.CertificadoNaoEncontradoException(cnpjId));

        return carregarKeyStore(certificado.getPfxData(), senha);
    }

    @Transactional(readOnly = true)
    public CertificadoA1 buscarInfo(UUID cnpjId) {
        UUID tenantId = TenantContext.get();

        return certificadoRepository
                .findByCnpjIdAndTenantId(cnpjId, tenantId)
                .orElseThrow(() -> new CertificadoException.CertificadoNaoEncontradoException(cnpjId));
    }

    @Transactional
    public void remover(UUID cnpjId) {
        UUID tenantId = TenantContext.get();

        CertificadoA1 certificado = certificadoRepository
                .findByCnpjIdAndTenantId(cnpjId, tenantId)
                .orElseThrow(() -> new CertificadoException.CertificadoNaoEncontradoException(cnpjId));

        certificadoRepository.delete(certificado);
    }

    // Helpers privados

    private KeyStore carregarKeyStore(byte[] pfxBytes, String senha) {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(pfxBytes), senha.toCharArray());
            return ks;
        } catch (Exception e) {
            throw new CertificadoException.SenhaInvalidaException();
        }
    }

    private LocalDateTime extrairValidade(KeyStore keyStore) {
        try {
            String alias = keyStore.aliases().nextElement();
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            Date notAfter = cert.getNotAfter();
            return notAfter.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível extrair a validade do certificado.", e);
        }
    }
}