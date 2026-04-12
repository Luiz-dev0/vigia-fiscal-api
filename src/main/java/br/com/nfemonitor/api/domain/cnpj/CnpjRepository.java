package br.com.nfemonitor.api.domain.cnpj;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CnpjRepository extends JpaRepository<Cnpj, UUID> {

    List<Cnpj> findAllByTenantId(UUID tenantId);

    List<Cnpj> findByTenantIdAndActiveTrue(UUID tenantId);

    Optional<Cnpj> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndCnpj(UUID tenantId, String cnpj);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndActiveTrue(UUID tenantId);

    List<Cnpj> findAllByActiveTrue();
}