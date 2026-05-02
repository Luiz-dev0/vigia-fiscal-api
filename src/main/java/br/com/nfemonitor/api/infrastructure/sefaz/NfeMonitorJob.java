package br.com.nfemonitor.api.infrastructure.sefaz;

import br.com.nfemonitor.api.domain.alert.AlertEventType;
import br.com.nfemonitor.api.domain.alert.AlertService;
import br.com.nfemonitor.api.domain.cnpj.Cnpj;
import br.com.nfemonitor.api.domain.cnpj.CnpjRepository;
import br.com.nfemonitor.api.domain.manifestacao.ManifestacaoService;
import br.com.nfemonitor.api.domain.nfe.NfeService;
import br.com.nfemonitor.api.domain.nfe.NotaFiscal;
import br.com.nfemonitor.api.domain.nfe.NfeStatus;
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
    private final AlertService alertService;
    private final ManifestacaoService manifestacaoService; // <-- novo

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

                List<NotaFiscal> nfes = nfeService.sincronizarPorCnpj(cnpj);
                consultationService.registrarSucesso(cnpj, nfes.size());

                nfes.forEach(this::processarNfe);

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

    private void processarNfe(NotaFiscal nfe) {
        // 1. Avalia alertas normalmente
        avaliarAlertas(nfe);

        // 2. Tenta enviar Ciência da Operação para notas AUTORIZADAS
        //    Isolado em try/catch próprio — nunca derruba o job
        if (nfe.getStatus() == NfeStatus.AUTORIZADA) {
            try {
                // senha null: cienciaAutomatica trata internamente e apenas loga
                // quando não há senha disponível, sem lançar exceção
                manifestacaoService.cienciaAutomatica(nfe, null);
            } catch (Exception e) {
                log.warn("[NfeMonitorJob] Falha inesperada na ciência automática para NF-e {}: {}",
                        nfe.getId(), e.getMessage());
            }
        }
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