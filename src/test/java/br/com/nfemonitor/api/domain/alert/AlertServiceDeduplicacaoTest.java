package br.com.nfemonitor.api.domain.alert;

import br.com.nfemonitor.api.domain.cnpj.Cnpj;
import br.com.nfemonitor.api.domain.nfe.NotaFiscal;
import br.com.nfemonitor.api.domain.tenant.Tenant;
import br.com.nfemonitor.api.infrastructure.notification.EmailSender;
import br.com.nfemonitor.api.infrastructure.notification.MensagemBuilder;
import br.com.nfemonitor.api.infrastructure.notification.WhatsAppSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService — mesmo evento nao dispara multiplos alertas")
class AlertServiceDeduplicacaoTest {

    @Mock private AlertRuleRepository alertRuleRepository;
    @Mock private AlertEventRepository alertEventRepository;
    @Mock private WhatsAppSender whatsAppSender;
    @Mock private EmailSender emailSender;
    @Mock private MensagemBuilder mensagemBuilder;

    @InjectMocks
    private AlertService alertService;

    private UUID tenantId;
    private UUID cnpjId;
    private NotaFiscal nfeMock;
    private AlertRule regraEmail;

    @BeforeEach
    void setup() {
        tenantId = UUID.randomUUID();
        cnpjId = UUID.randomUUID();

        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name("Tenant Teste")
                .email("tenant@teste.com.br")
                .trialEndsAt(LocalDateTime.now().plusDays(14))
                .build();

        Cnpj cnpj = Cnpj.builder()
                .id(cnpjId)
                .tenant(tenant)
                .cnpj("12345678000195")
                .razaoSocial("Empresa Teste SA")
                .uf("MT")
                .build();

        nfeMock = new NotaFiscal();
        nfeMock.setCnpj(cnpj);

        regraEmail = AlertRule.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .cnpjId(cnpjId)
                .eventType(AlertEventType.NOTA_REJEITADA)
                .channel(AlertChannel.EMAIL)
                .destination("contador@teste.com.br")
                .minutesBefore(0)
                .build();
    }

    @Test
    @DisplayName("Deve salvar e enviar alerta quando regra existe")
    void deveSalvarEEnviarAlertaQuandoRegraExiste() {
        when(alertRuleRepository.findRegrasAplicaveis(tenantId, cnpjId, AlertEventType.NOTA_REJEITADA))
                .thenReturn(List.of(regraEmail));
        when(alertEventRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mensagemBuilder.construir(any(), any())).thenReturn("mensagem teste");
        when(mensagemBuilder.construirAssuntoEmail(any(), any())).thenReturn("assunto teste");

        alertService.avaliarRegras(nfeMock, AlertEventType.NOTA_REJEITADA);

        verify(alertEventRepository, times(2)).save(any());
        verify(emailSender, times(1)).enviar(eq("contador@teste.com.br"), any(), any());
    }

    @Test
    @DisplayName("Nao deve enviar nada quando nao ha regras configuradas")
    void naoDeveEnviarNadaQuandoNaoHaRegras() {
        when(alertRuleRepository.findRegrasAplicaveis(tenantId, cnpjId, AlertEventType.NOTA_REJEITADA))
                .thenReturn(List.of());

        alertService.avaliarRegras(nfeMock, AlertEventType.NOTA_REJEITADA);

        verify(alertEventRepository, never()).save(any());
        verify(emailSender, never()).enviar(any(), any(), any());
        verify(whatsAppSender, never()).enviar(any(), any());
    }

    @Test
    @DisplayName("Duas regras devem gerar dois eventos independentes")
    void duasRegrasDeveriaGerarDoisEventos() {
        AlertRule regraWhatsapp = AlertRule.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .cnpjId(cnpjId)
                .eventType(AlertEventType.NOTA_REJEITADA)
                .channel(AlertChannel.WHATSAPP)
                .destination("+5565999999999")
                .minutesBefore(0)
                .build();

        when(alertRuleRepository.findRegrasAplicaveis(tenantId, cnpjId, AlertEventType.NOTA_REJEITADA))
                .thenReturn(List.of(regraEmail, regraWhatsapp));
        when(alertEventRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mensagemBuilder.construir(any(), any())).thenReturn("mensagem");
        when(mensagemBuilder.construirAssuntoEmail(any(), any())).thenReturn("assunto");

        alertService.avaliarRegras(nfeMock, AlertEventType.NOTA_REJEITADA);

        verify(emailSender, times(1)).enviar(any(), any(), any());
        verify(whatsAppSender, times(1)).enviar(any(), any());
    }
}