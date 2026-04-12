package br.com.nfemonitor.api.infrastructure.billing;

import br.com.nfemonitor.api.domain.tenant.Tenant;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripeClient {

    private final StripeProperties stripeProperties;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeProperties.getSecretKey();
        log.info("Stripe SDK inicializado");
    }

    public Customer criarCliente(Tenant tenant) throws StripeException {
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(tenant.getEmail())
                .setName(tenant.getName())
                .putMetadata("tenant_id", tenant.getId().toString())
                .build();
        return Customer.create(params);
    }

    public Subscription criarAssinatura(String customerId, String priceId) throws StripeException {
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams.Item.builder()
                        .setPrice(priceId)
                        .build())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .addExpand("latest_invoice.payment_intent")
                .build();
        return Subscription.create(params);
    }

    // FIX #2: cria uma Checkout Session do Stripe para o usuário completar o pagamento.
    // successUrl e cancelUrl devem apontar para o frontend (configurados via StripeProperties).
    public String criarCheckoutSession(String customerId, String priceId) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customerId)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build()
                )
                .setSuccessUrl(stripeProperties.getSuccessUrl())
                .setCancelUrl(stripeProperties.getCancelUrl())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    public Subscription cancelarAssinatura(String subscriptionId) throws StripeException {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        return subscription.cancel();
    }

    public Event construirWebhookEvent(String payload, String sigHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());
    }
}