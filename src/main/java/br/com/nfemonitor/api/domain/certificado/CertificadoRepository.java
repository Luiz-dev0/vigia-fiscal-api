package br.com.nfemonitor.api.domain.certificado;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CertificadoRepository extends JpaRepository<CertificadoA1, UUID> {

    Optional<CertificadoA1> findByCnpjId(UUID cnpjId);

    Optional<CertificadoA1> findByCnpjIdAndTenantId(UUID cnpjId, UUID tenantId);
}