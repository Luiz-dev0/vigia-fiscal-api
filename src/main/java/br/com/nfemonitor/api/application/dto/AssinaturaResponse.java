package br.com.nfemonitor.api.application.dto;

import br.com.nfemonitor.api.domain.billing.SubscriptionStatus;
import java.time.LocalDateTime;

// FIX #2: campo checkoutUrl adicionado.
// Retorna a URL do Stripe Checkout Session para o frontend redirecionar o usuário ao pagamento.
// Pode ser null em ambientes de teste sem Stripe real configurado.
public record AssinaturaResponse(
        String subscriptionId,
        SubscriptionStatus status,
        String plan,
        LocalDateTime currentPeriodEnd,
        String checkoutUrl
) {}