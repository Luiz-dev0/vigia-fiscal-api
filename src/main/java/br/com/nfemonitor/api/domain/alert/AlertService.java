package br.com.nfemonitor.api.domain.alert;

import br.com.nfemonitor.api.application.dto.AlertRuleRequest;
import br.com.nfemonitor.api.application.dto.AlertRuleResponse;
import br.com.nfemonitor.api.domain.nfe.NotaFiscal;
import br.com.nfemonitor.api.infrastructure.notification.EmailSender;
import br.com.nfemonitor.api.infrastructure.notification.MensagemBuilder;
import br.com.nfemonitor.api.infrastructure.notification.WhatsAppSender;
import br.com.nfemonitor.api.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRuleRepository alertRuleRepository;
    private final AlertEventRepository alertEventRepository;
    private final WhatsAppSender whatsAppSender;
    private final EmailSender emailSender;
    private final MensagemBuilder mensagemBuilder;

    public void avaliarRegras(NotaFiscal nfe, AlertEventType eventType) {
        UUID tenantId = nfe.getCnpj().getTenant().getId();
        UUID cnpjId = nfe.getCnpj().getId();

        List<AlertRule> regras = alertRuleRepository
                .findRegrasAplicaveis(tenantId, cnpjId, eventType);

        for (AlertRule regra : regras) {
            AlertEvent evento = new AlertEvent();
            evento.setAlertRule(regra);
            evento.setNotaFiscal(nfe);
            evento.setEventType(eventType);
            evento.setStatus(AlertEventStatus.PENDENTE_ENVIO);
            evento.setCriadoEm(LocalDateTime.now());
            alertEventRepository.save(evento);

            enviarNotificacao(evento, nfe, regra);
        }
    }

    private void enviarNotificacao(AlertEvent evento, NotaFiscal nfe, AlertRule regra) {
        String mensagem = mensagemBuilder.construir(evento, nfe);

        try {
            if (regra.getChannel() == AlertChannel.WHATSAPP) {
                whatsAppSender.enviar(regra.getDestination(), mensagem);
            } else if (regra.getChannel() == AlertChannel.EMAIL) {
                String assunto = mensagemBuilder.construirAssuntoEmail(evento, nfe);
                emailSender.enviar(regra.getDestination(), assunto, mensagem);
            }

            evento.setStatus(AlertEventStatus.ENVIADO);
            evento.setEnviadoEm(LocalDateTime.now());
            log.info("Alerta enviado. Evento ID: {}, Canal: {}", evento.getId(), regra.getChannel());

        } catch (Exception e) {
            evento.setStatus(AlertEventStatus.ERRO);
            evento.setErrorMsg(e.getMessage());
            log.error("Falha ao enviar alerta. Evento ID: {}, Erro: {}", evento.getId(), e.getMessage());
        } finally {
            alertEventRepository.save(evento);
        }
    }

    // CRUD chamado pelo AlertController
    public List<AlertRuleResponse> listar() {
        UUID tenantId = TenantContext.get();
        return alertRuleRepository.findByTenantIdAndActiveTrue(tenantId)
                .stream()
                .map(AlertRuleResponse::from)
                .toList();
    }

    public AlertRuleResponse buscar(UUID id) {
        UUID tenantId = TenantContext.get();
        AlertRule rule = alertRuleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada"));
        return AlertRuleResponse.from(rule);
    }

    public AlertRuleResponse criar(AlertRuleRequest request) {
        UUID tenantId = TenantContext.get();

        AlertRule rule = AlertRule.builder()
                .tenantId(tenantId)
                .cnpjId(request.cnpjId())
                .eventType(request.eventType())
                .channel(request.channel())
                .destination(request.destination())
                .minutesBefore(request.minutesBefore())
                .build();

        return AlertRuleResponse.from(alertRuleRepository.save(rule));
    }

    public void remover(UUID id) {
        alertRuleRepository.deleteById(id);
    }
}