package br.com.nfemonitor.api.domain.alert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AlertRule — tipos de evento e canais")
class AlertRuleEventTypeTest {

    @ParameterizedTest(name = "Deve criar regra com evento: {0}")
    @EnumSource(AlertEventType.class)
    @DisplayName("Todos os tipos de evento devem ser aceitos na regra")
    void todosOsTiposDeEventoDevemSerAceitos(AlertEventType tipo) {
        AlertRule regra = AlertRule.builder()
                .eventType(tipo)
                .channel(AlertChannel.EMAIL)
                .destination("teste@teste.com")
                .minutesBefore(0)
                .build();

        assertThat(regra.getEventType()).isEqualTo(tipo);
    }

    @Test
    @DisplayName("Canal WHATSAPP deve ser configurado corretamente")
    void canalWhatsappDeveSerConfigurado() {
        AlertRule regra = AlertRule.builder()
                .eventType(AlertEventType.PRAZO_CANCELAMENTO)
                .channel(AlertChannel.WHATSAPP)
                .destination("+5565999999999")
                .minutesBefore(30)
                .build();

        assertThat(regra.getChannel()).isEqualTo(AlertChannel.WHATSAPP);
        assertThat(regra.getDestination()).isEqualTo("+5565999999999");
        assertThat(regra.getMinutesBefore()).isEqualTo(30);
    }

    @Test
    @DisplayName("Canal EMAIL deve ser configurado corretamente")
    void canalEmailDeveSerConfigurado() {
        AlertRule regra = AlertRule.builder()
                .eventType(AlertEventType.NOTA_DENEGADA)
                .channel(AlertChannel.EMAIL)
                .destination("contador@escritorio.com.br")
                .minutesBefore(0)
                .build();

        assertThat(regra.getChannel()).isEqualTo(AlertChannel.EMAIL);
        assertThat(regra.getDestination()).isEqualTo("contador@escritorio.com.br");
    }

    @Test
    @DisplayName("Regra deve ser ativa por padrão")
    void regraDeveSerAtivaPorPadrao() {
        AlertRule regra = AlertRule.builder()
                .eventType(AlertEventType.NOTA_CANCELADA)
                .channel(AlertChannel.EMAIL)
                .destination("teste@teste.com")
                .minutesBefore(0)
                .build();

        assertThat(regra.isActive()).isTrue();
    }
}