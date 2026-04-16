package br.com.nfemonitor.api.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WhatsAppSender {

    @Value("${notification.mock:true}")
    private boolean mock;

    @Value("${notification.whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${notification.whatsapp.access-token:}")
    private String accessToken;

    private final RestTemplate restTemplate;

    public WhatsAppSender() {
        this.restTemplate = new RestTemplate();
    }

    public void enviar(String numero, String mensagem) {
        if (mock) {
            log.info("[MOCK] WhatsApp → {} | Mensagem: {}", numero, mensagem);
            return;
        }

        try {
            String url = "https://graph.facebook.com/v19.0/" + phoneNumberId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", numero,
                    "type", "text",
                    "text", Map.of("body", mensagem)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("WhatsApp enviado com sucesso para {}", numero);
            } else {
                log.warn("Falha ao enviar WhatsApp para {}. Status: {}", numero, response.getStatusCode());
                throw new RuntimeException("Status inesperado: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Erro ao enviar WhatsApp para {}: {}", numero, e.getMessage());
            throw new RuntimeException("Falha no envio WhatsApp: " + e.getMessage(), e);
        }
    }
}