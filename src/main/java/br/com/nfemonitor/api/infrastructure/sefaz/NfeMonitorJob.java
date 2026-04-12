package br.com.nfemonitor.api.infrastructure.sefaz;

import br.com.nfemonitor.api.domain.alert.AlertEventType;
import br.com.nfemonitor.api.domain.alert.AlertService;
import br.com.nfemonitor.api.domain.cnpj.Cnpj;
import br.com.nfemonitor.api.domain.cnpj.CnpjRepository;
import br.com.nfemonitor.api.domain.nfe.NfeService;
import br.com.nfemonitor.api.domain.nfe.NotaFiscal;
import br.com.nfemonitor.api.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NfeMonitorJob {

    private final CnpjRepository cnpjRepository;
    private final NfeService nfeService;
    private final SefazConsultationService consultationService;
    private final AlertService alertService; // <-- novo

    @Scheduled(fixedDelay = 300000)
    public void executar() {
        log.info("[NfeMonitorJob] Iniciando ciclo de monitoramento...");

        List<Cnpj> cnpjsAtivos = cnpjRepository.findAllByActiveTrue();
        log.info("[NfeMonitorJob] {} CNPJ(s) ativo(s) encontrado(s)", cnpjsAtivos.size());

        for (Cnpj cnpj : cnpjsAtivos) {
            try {
                TenantContext.set(cnpj.getTenant().getId());
                log.info("[NfeMonitorJob] Processando CNPJ {} do tenant {}",
                        cnpj.getCnpj(), cnpj.getTenant().getId());

                // agora recebe a lista de NF-es sincronizadas
                List<NotaFiscal> nfes = nfeService.sincronizarPorCnpj(cnpj);
                consultationService.registrarSucesso(cnpj, nfes.size());

                // avalia alertas para cada NF-e
                nfes.forEach(this::avaliarAlertas);

            } catch (Exception e) {
                log.error("[NfeMonitorJob] Falha ao processar CNPJ {}: {}",
                        cnpj.getCnpj(), e.getMessage());
                consultationService.registrarErro(cnpj, e.getMessage());

            } finally {
                TenantContext.clear();
            }
        }

        log.info("[NfeMonitorJob] Ciclo de monitoramento concluído.");
    }

    private void avaliarAlertas(NotaFiscal nfe) {
        switch (nfe.getStatus()) {
            case REJEITADA -> alertService.avaliarRegras(nfe, AlertEventType.NOTA_REJEITADA);
            case CANCELADA -> alertService.avaliarRegras(nfe, AlertEventType.NOTA_CANCELADA);
            case DENEGADA  -> alertService.avaliarRegras(nfe, AlertEventType.NOTA_DENEGADA);
            default -> log.debug("[NfeMonitorJob] Status {} não dispara alerta para NF-e {}",
                    nfe.getStatus(), nfe.getId());
        }
    }
}