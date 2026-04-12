package br.com.nfemonitor.api.domain.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertEventRepository extends JpaRepository<AlertEvent, UUID> {

    List<AlertEvent> findByAlertRuleIdOrderByEnviadoEmDesc(UUID alertRuleId);

    boolean existsByAlertRuleIdAndNotaFiscalId(UUID alertRuleId, UUID notaFiscalId);
}