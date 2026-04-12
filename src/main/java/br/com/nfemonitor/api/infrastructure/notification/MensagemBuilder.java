package br.com.nfemonitor.api.infrastructure.notification;

import br.com.nfemonitor.api.domain.alert.AlertEvent;
import br.com.nfemonitor.api.domain.alert.AlertEventType;
import br.com.nfemonitor.api.domain.nfe.NotaFiscal;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class MensagemBuilder {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String construir(AlertEvent evento, NotaFiscal nfe) {
        AlertEventType tipo = evento.getEventType();
        String chave = nfe.getChaveAcesso() != null ? nfe.getChaveAcesso() : "N/A";
        String cnpj = nfe.getCnpj() != null ? nfe.getCnpj().getCnpj() : "N/A";
        String status = nfe.getStatus() != null ? nfe.getStatus().name() : "N/A";
        String dataEmissao = nfe.getDataEmissao() != null
                ? nfe.getDataEmissao().format(FORMATTER)
                : "N/A";

        return switch (tipo) {
            case NOTA_CANCELADA -> String.format("""
                    ⚠️ *NF-e CANCELADA*
                    
                    CNPJ: %s
                    Chave: %s
                    Data Emissão: %s
                    
                    A nota fiscal foi cancelada na SEFAZ.
                    """, cnpj, chave, dataEmissao);

            case NOTA_DENEGADA -> String.format("""
                    🚫 *NF-e DENEGADA*
                    
                    CNPJ: %s
                    Chave: %s
                    Data Emissão: %s
                    
                    A nota fiscal foi denegada pela SEFAZ.
                    """, cnpj, chave, dataEmissao);

            case NOTA_REJEITADA -> String.format("""
                    ❌ *NF-e REJEITADA*
                    
                    CNPJ: %s
                    Chave: %s
                    Status: %s
                    """, cnpj, chave, status);

            case PRAZO_CANCELAMENTO -> String.format("""
                    ⏰ *PRAZO DE CANCELAMENTO SE APROXIMANDO*
                    
                    CNPJ: %s
                    Chave: %s
                    Data Emissão: %s
                    
                    O prazo para cancelamento está próximo do vencimento.
                    """, cnpj, chave, dataEmissao);
        };
    }

    public String construirAssuntoEmail(AlertEvent evento, NotaFiscal nfe) {
        String cnpj = nfe.getCnpj() != null ? nfe.getCnpj().getCnpj() : "N/A";
        return switch (evento.getEventType()) {
            case NOTA_CANCELADA -> "[NF-e Monitor] NF-e Cancelada — CNPJ " + cnpj;
            case NOTA_DENEGADA -> "[NF-e Monitor] NF-e Denegada — CNPJ " + cnpj;
            case NOTA_REJEITADA -> "[NF-e Monitor] NF-e Rejeitada — CNPJ " + cnpj;
            case PRAZO_CANCELAMENTO -> "[NF-e Monitor] Prazo de cancelamento próximo — CNPJ " + cnpj;
        };
    }
}
