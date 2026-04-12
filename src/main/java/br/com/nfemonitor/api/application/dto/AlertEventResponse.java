package br.com.nfemonitor.api.application.dto;

import br.com.nfemonitor.api.domain.alert.AlertEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record AlertEventResponse(
        UUID id,
        UUID ruleId,
        UUID notaFiscalId,
        String eventType,
        String status,
        LocalDateTime enviadoEm,
        String errorMsg,
        LocalDateTime criadoEm
) {
    public static AlertEventResponse from(AlertEvent event) {
        return new AlertEventResponse(
                event.getId(),
                event.getAlertRule() != null ? event.getAlertRule().getId() : null,
                event.getNotaFiscal() != null ? event.getNotaFiscal().getId() : null,
                event.getEventType() != null ? event.getEventType().name() : null,
                event.getStatus() != null ? event.getStatus().name() : null,
                event.getEnviadoEm(),
                event.getErrorMsg(),
                event.getCriadoEm()
        );
    }
}