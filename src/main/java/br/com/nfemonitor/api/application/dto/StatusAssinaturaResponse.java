package br.com.nfemonitor.api.application.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record StatusAssinaturaResponse(
        /**
         * Valores possíveis: "TRIAL" | "TRIAL_EXPIRED" | "ACTIVE" | "OVERDUE" | "CANCELLED" | "INCOMPLETE"
         */
        String estado,
        String plano,
        LocalDateTime currentPeriodEnd,
        LocalDateTime trialEndsAt,
        Long diasRestantes
) {}