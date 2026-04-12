package br.com.nfemonitor.api.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailSender {

    @Value("${notification.mock:true}")
    private boolean mock;

    @Value("${notification.email.remetente:nfemonitor@seudominio.com}")
    private String remetente;

    private final JavaMailSender mailSender;

    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviar(String destinatario, String assunto, String corpo) {
        if (mock) {
            log.info("[MOCK] Email → {} | Assunto: {} | Corpo: {}", destinatario, assunto, corpo);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetente);
            message.setTo(destinatario);
            message.setSubject(assunto);
            message.setText(corpo);

            mailSender.send(message);
            log.info("Email enviado com sucesso para {}", destinatario);
        } catch (Exception e) {
            log.error("Erro ao enviar email para {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Falha no envio de email: " + e.getMessage(), e);
        }
    }
}