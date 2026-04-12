package br.com.nfemonitor.api.domain.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRuleRepository extends JpaRepository<AlertRule, UUID> {

    List<AlertRule> findByTenantIdAndActiveTrue(UUID tenantId);

    Optional<AlertRule> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("""
        SELECT r FROM AlertRule r
        WHERE r.tenantId = :tenantId
          AND r.eventType = :eventType
          AND r.active = true
          AND (r.cnpjId = :cnpjId OR r.cnpjId IS NULL)
    """)
    List<AlertRule> findRegrasAplicaveis(
            @Param("tenantId") UUID tenantId,
            @Param("cnpjId") UUID cnpjId,
            @Param("eventType") AlertEventType eventType
    );
}