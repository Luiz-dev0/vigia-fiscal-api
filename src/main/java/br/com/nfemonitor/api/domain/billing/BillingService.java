package br.com.nfemonitor.api.domain.billing;

import br.com.nfemonitor.api.domain.tenant.Tenant;
import br.com.nfemonitor.api.domain.tenant.TenantRepository;
import br.com.nfemonitor.api.infrastructure.billing.StripeClient;
import br.com.nfemonitor.api.infrastructure.billing.StripeProperties;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final SubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;
    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;

    @Transactional
    public br.com.nfemonitor.api.domain.billing.Subscription assinar(UUID tenantId, String plan) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));

        // FIX #1: normaliza o plano removendo acentos antes de buscar no mapa do Stripe.
        // Ex: "BÁSICO" → "basico", "PRO" → "pro", "ENTERPRISE" → "enterprise".
        // Sem isso, "Básico".toUpperCase() = "BÁSICO" e o lookup retornava null.
        String planKey = Normalizer
                .normalize(plan.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        String priceId = stripeProperties.getPriceId().get(planKey);
        if (priceId == null || priceId.isBlank()) {
            throw new RuntimeException("Plano inválido ou sem priceId configurado: " + plan);
        }

        try {
            String customerId;
            var existingSubscription = subscriptionRepository.findByTenantId(tenantId);
            if (existingSubscription.isPresent() && existingSubscription.get().getStripeCustomerId() != null) {
                customerId = existingSubscription.get().getStripeCustomerId();
            } else {
                Customer customer = stripeClient.criarCliente(tenant);
                customerId = customer.getId();
            }

            Subscription stripeSubscription = stripeClient.criarAssinatura(customerId, priceId);

            var subscription = existingSubscription.orElse(
                    br.com.nfemonitor.api.domain.billing.Subscription.builder()
                            .tenant(tenant)
                            .build()
            );

            subscription.setPlan(plan.toUpperCase());
            subscription.setStatus(SubscriptionStatus.INCOMPLETE);
            subscription.setStripeCustomerId(customerId);
            subscription.setStripeSubscriptionId(stripeSubscription.getId());
            subscription.setStripePriceId(priceId);

            if (stripeSubscription.getCurrentPeriodEnd() != null) {
                subscription.setCurrentPeriodEnd(
                        LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(stripeSubscription.getCurrentPeriodEnd()),
                                ZoneId.systemDefault()
                        )
                );
            }

            return subscriptionRepository.save(subscription);

        } catch (Exception e) {
            log.error("Erro ao criar assinatura para tenant {}: {}", tenantId, e.getMessage());
            throw new RuntimeException("Falha ao processar assinatura: " + e.getMessage());
        }
    }

    @Transactional
    public void cancelar(UUID tenantId) {
        var subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Assinatura não encontrada"));

        try {
            stripeClient.cancelarAssinatura(subscription.getStripeSubscriptionId());
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(subscription);
        } catch (Exception e) {
            log.error("Erro ao cancelar assinatura para tenant {}: {}", tenantId, e.getMessage());
            throw new RuntimeException("Falha ao cancelar assinatura: " + e.getMessage());
        }
    }

    @Transactional
    public void processarWebhook(String payload, String sigHeader) {
        try {
            Event event = stripeClient.construirWebhookEvent(payload, sigHeader);
            log.info("Webhook Stripe recebido: {}", event.getType());

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            switch (event.getType()) {
                case "customer.subscription.updated" -> {
                    deserializer.getObject().ifPresent(obj -> {
                        Subscription sub = (Subscription) obj;
                        atualizarAssinatura(sub);
                    });
                }
                case "customer.subscription.deleted" -> {
                    deserializer.getObject().ifPresent(obj -> {
                        Subscription sub = (Subscription) obj;
                        subscriptionRepository.findByStripeSubscriptionId(sub.getId())
                                .ifPresent(s -> {
                                    s.setStatus(SubscriptionStatus.CANCELLED);
                                    subscriptionRepository.save(s);
                                    sincronizarTenantCancelado(s.getTenant().getId());
                                    log.info("Assinatura cancelada via webhook: {}", sub.getId());
                                });
                    });
                }
                case "invoice.payment_succeeded" -> {
                    deserializer.getObject().ifPresent(obj -> {
                        Invoice invoice = (Invoice) obj;
                        String subscriptionId = invoice.getSubscription();
                        subscriptionRepository.findByStripeSubscriptionId(subscriptionId)
                                .ifPresent(s -> {
                                    s.setStatus(SubscriptionStatus.ACTIVE);
                                    subscriptionRepository.save(s);
                                    sincronizarTenantAtivo(s.getTenant().getId(), s.getPlan());
                                    log.info("Pagamento confirmado, assinatura ativa: {}", subscriptionId);
                                });
                    });
                }
                case "invoice.payment_failed" -> {
                    deserializer.getObject().ifPresent(obj -> {
                        Invoice invoice = (Invoice) obj;
                        String subscriptionId = invoice.getSubscription();
                        subscriptionRepository.findByStripeSubscriptionId(subscriptionId)
                                .ifPresent(s -> {
                                    s.setStatus(SubscriptionStatus.OVERDUE);
                                    subscriptionRepository.save(s);
                                    log.warn("Pagamento falhou, assinatura inadimplente: {}", subscriptionId);
                                });
                    });
                }
                default -> log.debug("Evento Stripe ignorado: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("Erro ao processar webhook: {}", e.getMessage());
            throw new RuntimeException("Webhook inválido: " + e.getMessage());
        }
    }

    public Optional<br.com.nfemonitor.api.domain.billing.Subscription> buscarAssinaturaAtiva(UUID tenantId) {
        return subscriptionRepository.findByTenantId(tenantId);
    }

    private void atualizarAssinatura(Subscription stripeSubscription) {
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscription.getId())
                .ifPresent(s -> {
                    SubscriptionStatus novoStatus = switch (stripeSubscription.getStatus()) {
                        case "active" -> SubscriptionStatus.ACTIVE;
                        case "canceled" -> SubscriptionStatus.CANCELLED;
                        case "incomplete", "incomplete_expired" -> SubscriptionStatus.INCOMPLETE;
                        case "past_due", "unpaid" -> SubscriptionStatus.OVERDUE;
                        default -> s.getStatus();
                    };
                    s.setStatus(novoStatus);

                    if (stripeSubscription.getCurrentPeriodEnd() != null) {
                        s.setCurrentPeriodEnd(
                                LocalDateTime.ofInstant(
                                        Instant.ofEpochSecond(stripeSubscription.getCurrentPeriodEnd()),
                                        ZoneId.systemDefault()
                                )
                        );
                    }
                    subscriptionRepository.save(s);

                    if (novoStatus == SubscriptionStatus.ACTIVE) {
                        sincronizarTenantAtivo(s.getTenant().getId(), s.getPlan());
                    } else if (novoStatus == SubscriptionStatus.CANCELLED) {
                        sincronizarTenantCancelado(s.getTenant().getId());
                    }

                    log.info("Assinatura atualizada: {} → {}", stripeSubscription.getId(), novoStatus);
                });
    }

    private void sincronizarTenantAtivo(UUID tenantId, String plano) {
        tenantRepository.findById(tenantId).ifPresent(tenant -> {
            tenant.setPlan(plano);
            tenant.setPlanStatus("ACTIVE");
            tenantRepository.save(tenant);
            log.info("Tenant {} atualizado para plano {}", tenantId, plano);
        });
    }

    private void sincronizarTenantCancelado(UUID tenantId) {
        tenantRepository.findById(tenantId).ifPresent(tenant -> {
            tenant.setPlan("TRIAL");
            tenant.setPlanStatus("CANCELLED");
            tenantRepository.save(tenant);
            log.info("Tenant {} revertido para TRIAL após cancelamento", tenantId);
        });
    }
}