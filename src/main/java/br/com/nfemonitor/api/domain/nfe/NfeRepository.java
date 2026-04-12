package br.com.nfemonitor.api.domain.nfe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface NfeRepository extends JpaRepository<NotaFiscal, UUID> {

    // Métodos existentes — mantidos intactos
    List<NotaFiscal> findAllByTenantId(UUID tenantId);

    List<NotaFiscal> findAllByTenantIdAndCnpjId(UUID tenantId, UUID cnpjId);

    Optional<NotaFiscal> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<NotaFiscal> findByChaveAcesso(String chaveAcesso);

    boolean existsByChaveAcesso(String chaveAcesso);

    // usado pelo NfeRetencaoJob
    // "n.tenant.id" navega pelo @ManyToOne Tenant para chegar ao UUID
    @Modifying
    @Query("""
        DELETE FROM NotaFiscal n
        WHERE n.tenant.id = :tenantId
          AND n.dataEmissao < :corte
        """)
    int deleteByTenantIdAndDataEmissaoBefore(
            @Param("tenantId") UUID tenantId,
            @Param("corte")    LocalDateTime corte
    );
}