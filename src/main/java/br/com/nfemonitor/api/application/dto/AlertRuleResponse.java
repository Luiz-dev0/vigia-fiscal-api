package br.com.nfemonitor.api.application.dto;

import br.com.nfemonitor.api.domain.alert.AlertChannel;
import br.com.nfemonitor.api.domain.alert.AlertEventType;
import br.com.nfemonitor.api.domain.alert.AlertRule;

import java.time.LocalDateTime;
import java.util.UUID;

public record AlertRuleResponse(
        UUID id,
        UUID tenantId,
        UUID cnpjId,
        AlertEventType eventType,
        AlertChannel channel,
        String destination,
        Integer minutesBefore,
        boolean active,
        LocalDateTime createdAt
) {
    public static AlertRuleResponse from(AlertRule rule) {
        return new AlertRuleResponse(
                rule.getId(),
                rule.getTenantId(),
                rule.getCnpjId(),
                rule.getEventType(),
                rule.getChannel(),
                rule.getDestination(),
                rule.getMinutesBefore(),
                rule.isActive(),
                rule.getCreatedAt()
        );
    }
}