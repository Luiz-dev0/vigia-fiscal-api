package br.com.nfemonitor.api.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class WhatsAppSender {

    @Value("${notification.mock:true}")
    private boolean mock;

    @Value("${notification.whatsapp.evolution-api-url:http://localhost:8080}")
    private String evolutionApiUrl;

    @Value("${notification.whatsapp.evolution-api-key:}")
    private String evolutionApiKey;

    @Value("${notification.whatsapp.instance-name:NFeMonitor}")
    private String instanceName;

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
            String url = evolutionApiUrl + "/message/sendText/" + instanceName;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", evolutionApiKey);

            Map<String, Object> body = Map.of(
                    "number", numero,
                    "text", mensagem
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