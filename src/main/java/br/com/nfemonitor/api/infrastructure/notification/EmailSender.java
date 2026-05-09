package br.com.nfemonitor.api.infrastructure.notification;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailSender {

    @Value("${notification.mock:true}")
    private boolean mock;

    @Value("${notification.email.remetente:noreply@vigiafiscal.com.br}")
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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(remetente, "Vigia Fiscal"));
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpo, false);

            mailSender.send(message);
            log.info("Email enviado com sucesso para {}", destinatario);
        } catch (Exception e) {
            log.error("Erro ao enviar email para {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Falha no envio de email: " + e.getMessage(), e);
        }
    }
}