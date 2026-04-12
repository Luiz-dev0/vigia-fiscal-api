package br.com.nfemonitor.api.infrastructure.sefaz;

import br.com.nfemonitor.api.domain.billing.Subscription;
import br.com.nfemonitor.api.domain.billing.SubscriptionRepository;
import br.com.nfemonitor.api.domain.billing.SubscriptionStatus;
import br.com.nfemonitor.api.domain.nfe.NfeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Aplica política de retenção de NF-e por plano.
 * Executa diariamente às 03:00 (horário do servidor).
 *
 *   BASICO     → mantém últimos 3 meses
 *   PRO        → mantém últimos 12 meses
 *   ENTERPRISE → sem purge (retenção ilimitada)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NfeRetencaoJob {

    private final NfeRepository nfeRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void aplicarPoliticaDeRetencao() {
        log.info("[NfeRetencaoJob] Iniciando purge de NF-e por política de retenção...");

        List<Subscription> assinaturas = subscriptionRepository
                .findAllByStatus(SubscriptionStatus.ACTIVE);

        int totalPurgado = 0;

        for (Subscription assinatura : assinaturas) {
            UUID tenantId  = assinatura.getTenant().getId();
            String plano   = assinatura.getPlan();

            LocalDateTime corte = calcularCorte(plano);
            if (corte == null) continue; // ENTERPRISE — sem purge

            int deletados = nfeRepository
                    .deleteByTenantIdAndDataEmissaoBefore(tenantId, corte);

            totalPurgado += deletados;

            if (deletados > 0) {
                log.info("[NfeRetencaoJob] tenant={} plano={} → {} NF-e removidas",
                        tenantId, plano, deletados);
            }
        }

        log.info("[NfeRetencaoJob] Concluído. Total removido: {}", totalPurgado);
    }

    /** null = sem corte (ENTERPRISE) */
    private LocalDateTime calcularCorte(String plano) {
        LocalDateTime agora = LocalDateTime.now();
        return switch (plano.toUpperCase()) {
            case "BASICO"     -> agora.minusMonths(3);
            case "PRO"        -> agora.minusMonths(12);
            case "ENTERPRISE" -> null;
            default -> {
                log.warn("[NfeRetencaoJob] Plano desconhecido '{}' — aplicando 3 meses.", plano);
                yield agora.minusMonths(3);
            }
        };
    }
}