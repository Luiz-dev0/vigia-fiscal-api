package br.com.vigiafiscal.api.application.dto;

import br.com.vigiafiscal.api.domain.alert.AlertChannel;
import br.com.vigiafiscal.api.domain.alert.AlertEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AlertRuleRequest(
        UUID cnpjId,

        @NotNull
        AlertEventType eventType,

        @NotNull
        AlertChannel channel,

        @NotBlank
        String destination,

        @NotNull
        Integer minutesBefore
) {}
