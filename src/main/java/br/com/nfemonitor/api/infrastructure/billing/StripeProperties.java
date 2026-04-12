package br.com.nfemonitor.api.infrastructure.billing;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    private String secretKey;
    private String webhookSecret;

    // FIX #2: URLs para redirecionar após o pagamento no Stripe Checkout.
    // Configure em application.yml (dev) e application-prod.yml (produção).
    private String successUrl;
    private String cancelUrl;

    // Mapa de plano → priceId do Stripe.
    // Chaves esperadas: "basico", "pro", "enterprise" (sem acento, minúsculo).
    private Map<String, String> priceId;
}