package br.com.nfemonitor.api.application.rest;

import br.com.nfemonitor.api.application.dto.AssinaturaRequest;
import br.com.nfemonitor.api.application.dto.AssinaturaResponse;
import br.com.nfemonitor.api.application.dto.PlanoResponse;
import br.com.nfemonitor.api.application.dto.StatusAssinaturaResponse;
import br.com.nfemonitor.api.domain.billing.BillingService;
import br.com.nfemonitor.api.domain.billing.Subscription;
import br.com.nfemonitor.api.domain.billing.SubscriptionStatus;
import br.com.nfemonitor.api.domain.tenant.Tenant;
import br.com.nfemonitor.api.domain.tenant.TenantRepository;
import br.com.nfemonitor.api.infrastructure.billing.StripeClient;
import br.com.nfemonitor.api.infrastructure.billing.StripeProperties;
import br.com.nfemonitor.api.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;
    private final TenantRepository tenantRepository;

    @GetMapping("/planos")
    public ResponseEntity<List<PlanoResponse>> listarPlanos() {
        var priceIds = stripeProperties.getPriceId();

        List<PlanoResponse> planos = List.of(
                new PlanoResponse(
                        "Básico",
                        97,
                        1,
                        "Para monitoramento individual simples.",
                        priceIds.getOrDefault("basico", "")
                ),
                new PlanoResponse(
                        "Pro",
                        197,
                        5,
                        "Escalabilidade para pequenos escritórios.",
                        priceIds.getOrDefault("pro", "")
                ),
                new PlanoResponse(
                        "Enterprise",
                        497,
                        0,
                        "Potência total para grandes operações.",
                        priceIds.getOrDefault("enterprise", "")
                )
        );

        return ResponseEntity.ok(planos);
    }

    @GetMapping("/status")
    public ResponseEntity<StatusAssinaturaResponse> status() {
        var tenantId = TenantContext.get();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));

        var subscriptionOpt = billingService.buscarAssinaturaAtiva(tenantId);

        if (subscriptionOpt.isPresent()) {
            var sub = subscriptionOpt.get();
            if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
                return ResponseEntity.ok(StatusAssinaturaResponse.builder()
                        .estado("ACTIVE")
                        .plano(sub.getPlan())
                        .currentPeriodEnd(sub.getCurrentPeriodEnd())
                        .trialEndsAt(null)
                        .diasRestantes(null)
                        .build());
            }
            return ResponseEntity.ok(StatusAssinaturaResponse.builder()
                    .estado(sub.getStatus().name())
                    .plano(sub.getPlan())
                    .currentPeriodEnd(sub.getCurrentPeriodEnd())
                    .trialEndsAt(null)
                    .diasRestantes(null)
                    .build());
        }

        if (tenant.isTrialValido()) {
            long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDateTime.now(), tenant.getTrialEndsAt());
            return ResponseEntity.ok(StatusAssinaturaResponse.builder()
                    .estado("TRIAL")
                    .plano("TRIAL")
                    .currentPeriodEnd(null)
                    .trialEndsAt(tenant.getTrialEndsAt())
                    .diasRestantes(diasRestantes)
                    .build());
        }

        return ResponseEntity.ok(StatusAssinaturaResponse.builder()
                .estado("TRIAL_EXPIRED")
                .plano(null)
                .currentPeriodEnd(null)
                .trialEndsAt(tenant.getTrialEndsAt())
                .diasRestantes(0L)
                .build());
    }

    @PostMapping("/assinar")
    public ResponseEntity<AssinaturaResponse> assinar(@RequestBody AssinaturaRequest request) {
        var tenantId = TenantContext.get();
        Subscription subscription = billingService.assinar(tenantId, request.plan());

        // FIX #2: cria Checkout Session do Stripe e retorna a URL para o frontend redirecionar.
        // Se falhar (ex: ambiente de teste sem Stripe configurado), retorna checkoutUrl = null
        // e o frontend continua normalmente sem redirecionar.
        String checkoutUrl = null;
        try {
            // Normaliza planKey para buscar priceId (mesmo critério do BillingService)
            String planKey = Normalizer
                    .normalize(request.plan().toLowerCase(), Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            String priceId = stripeProperties.getPriceId().get(planKey);
            checkoutUrl = stripeClient.criarCheckoutSession(
                    subscription.getStripeCustomerId(), priceId);
        } catch (Exception e) {
            log.warn("Checkout Session não criada (Stripe indisponível?): {}", e.getMessage());
        }

        return ResponseEntity.ok(new AssinaturaResponse(
                subscription.getStripeSubscriptionId(),
                subscription.getStatus(),
                subscription.getPlan(),
                subscription.getCurrentPeriodEnd(),
                checkoutUrl
        ));
    }

    @DeleteMapping("/cancelar")
    public ResponseEntity<Void> cancelar() {
        var tenantId = TenantContext.get();
        billingService.cancelar(tenantId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws IOException {
        String payload = new String(request.getInputStream().readAllBytes());
        billingService.processarWebhook(payload, sigHeader);
        return ResponseEntity.ok("ok");
    }
}