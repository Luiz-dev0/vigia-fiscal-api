package br.com.vigiafiscal.api.infrastructure.sefaz;

import br.com.vigiafiscal.api.domain.alert.AlertEventType;
import br.com.vigiafiscal.api.domain.alert.AlertService;
import br.com.vigiafiscal.api.domain.cnpj.Cnpj;
import br.com.vigiafiscal.api.domain.cnpj.CnpjRepository;
import br.com.vigiafiscal.api.domain.manifestacao.ManifestacaoService;
import br.com.vigiafiscal.api.domain.nfe.NfeService;
import br.com.vigiafiscal.api.domain.nfe.NotaFiscal;
import br.com.vigiafiscal.api.domain.nfe.NfeStatus;
import br.com.vigiafiscal.api.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NfeVigiaJob {

    private final CnpjRepository cnpjRepository;
    private final NfeService nfeService;
    private final SefazConsultationService consultationService;
    private final AlertService alertService;
    private final ManifestacaoService manifestacaoService; // <-- novo

    @Scheduled(fixedDelay = 300000)
    public void executar() {
        log.info("[NfeVigiaJob] Iniciando ciclo de monitoramento...");

        List<Cnpj> cnpjsAtivos = cnpjRepository.findAllByActiveTrue();
        log.info("[NfeVigiaJob] {} CNPJ(s) ativo(s) encontrado(s)", cnpjsAtivos.size());

        for (Cnpj cnpj : cnpjsAtivos) {
            try {
                TenantContext.set(cnpj.getTenant().getId());
                log.info("[NfeVigiaJob] Processando CNPJ {} do tenant {}",
                        cnpj.getCnpj(), cnpj.getTenant().getId());

                List<NotaFiscal> nfes = nfeService.sincronizarPorCnpj(cnpj);
                consultationService.registrarSucesso(cnpj, nfes.size());

                nfes.forEach(this::processarNfe);

            } catch (Exception e) {
                log.error("[NfeVigiaJob] Falha ao processar CNPJ {}: {}",
                        cnpj.getCnpj(), e.getMessage());
                consultationService.registrarErro(cnpj, e.getMessage());

            } finally {
                TenantContext.clear();
            }
        }

        log.info("[NfeVigiaJob] Ciclo de monitoramento concluído.");
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
                log.warn("[NfeVigiaJob] Falha inesperada na ciência automática para NF-e {}: {}",
                        nfe.getId(), e.getMessage());
            }
        }
    }

    private void avaliarAlertas(NotaFiscal nfe) {
        switch (nfe.getStatus()) {
            case REJEITADA -> alertService.avaliarRegras(nfe, AlertEventType.NOTA_REJEITADA);
            case CANCELADA -> alertService.avaliarRegras(nfe, AlertEventType.NOTA_CANCELADA);
            case DENEGADA  -> alertService.avaliarRegras(nfe, AlertEventType.NOTA_DENEGADA);
            default -> log.debug("[NfeVigiaJob] Status {} não dispara alerta para NF-e {}",
                    nfe.getStatus(), nfe.getId());
        }
    }
}
