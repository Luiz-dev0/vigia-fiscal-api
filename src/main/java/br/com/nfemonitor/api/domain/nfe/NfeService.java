package br.com.nfemonitor.api.domain.nfe;

import br.com.nfemonitor.api.application.dto.NfeResponse;
import br.com.nfemonitor.api.domain.cnpj.Cnpj;
import br.com.nfemonitor.api.domain.cnpj.CnpjRepository;
import br.com.nfemonitor.api.domain.sefaz.SefazConsulta;
import br.com.nfemonitor.api.domain.sefaz.SefazConsultaRepository;
import br.com.nfemonitor.api.domain.tenant.Tenant;
import br.com.nfemonitor.api.domain.tenant.TenantRepository;
import br.com.nfemonitor.api.infrastructure.sefaz.NfeDados;
import br.com.nfemonitor.api.infrastructure.sefaz.SefazClient;
import br.com.nfemonitor.api.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NfeService {

    private final NfeRepository nfeRepository;
    private final CnpjRepository cnpjRepository;
    private final TenantRepository tenantRepository;
    private final SefazConsultaRepository sefazConsultaRepository;
    private final SefazClient sefazClient;

    public List<NfeResponse> listarTodas() {
        UUID tenantId = TenantContext.get();
        return nfeRepository.findAllByTenantId(tenantId)
                .stream()
                .map(NfeResponse::from)
                .toList();
    }

    public List<NfeResponse> listarPorCnpj(UUID cnpjId) {
        UUID tenantId = TenantContext.get();
        validarCnpjDoTenant(cnpjId, tenantId);
        return nfeRepository.findAllByTenantIdAndCnpjId(tenantId, cnpjId)
                .stream()
                .map(NfeResponse::from)
                .toList();
    }

    public NfeResponse buscarPorId(UUID id) {
        UUID tenantId = TenantContext.get();
        return nfeRepository.findByIdAndTenantId(id, tenantId)
                .map(NfeResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "NF-e não encontrada"));
    }

    @Transactional
    public List<NfeResponse> sincronizarNfesDoCnpj(UUID cnpjId) {
        UUID tenantId = TenantContext.get();
        Cnpj cnpj = validarCnpjDoTenant(cnpjId, tenantId);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tenant não encontrado"));

        SefazConsulta consulta = SefazConsulta.builder()
                .tenant(tenant)
                .cnpj(cnpj)
                .tipo("CONSULTA_DISTRIBUICAO_DFE")
                .build();

        try {
            log.info("Iniciando consulta SEFAZ para CNPJ: {}", cnpj.getCnpj());
            List<NfeDados> notasRecebidas = sefazClient.consultarNfes(cnpj.getCnpj());
            List<NotaFiscal> notasSalvas = notasRecebidas.stream()
                    .map(dados -> upsertNfe(dados, cnpj, tenant))
                    .toList();

            consulta.setSucesso(true);
            consulta.setStatusHttp(200);
            consulta.setNotasEncontradas(notasSalvas.size());
            consulta.setMensagem("Consulta realizada com sucesso.");
            consulta.setFinalizadoEm(LocalDateTime.now());
            sefazConsultaRepository.save(consulta);

            log.info("Sincronizadas {} NF-es para CNPJ: {}", notasSalvas.size(), cnpj.getCnpj());
            return notasSalvas.stream().map(NfeResponse::from).toList();

        } catch (Exception e) {
            consulta.setSucesso(false);
            consulta.setMensagem("Erro: " + e.getMessage());
            consulta.setFinalizadoEm(LocalDateTime.now());
            sefazConsultaRepository.save(consulta);
            log.error("Erro na consulta SEFAZ para CNPJ {}: {}", cnpj.getCnpj(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Falha ao consultar SEFAZ: " + e.getMessage());
        }
    }

    /**
     * Método usado pelo NfeMonitorJob.
     * Retorna a lista de NF-es sincronizadas para avaliação de alertas.
     */
    @Transactional
    public List<NotaFiscal> sincronizarPorCnpj(Cnpj cnpj) {
        Tenant tenant = tenantRepository.findById(cnpj.getTenant().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tenant não encontrado"));

        SefazConsulta consulta = SefazConsulta.builder()
                .tenant(tenant)
                .cnpj(cnpj)
                .tipo("CONSULTA_DISTRIBUICAO_DFE")
                .build();

        try {
            log.info("[Job] Consultando SEFAZ para CNPJ: {}", cnpj.getCnpj());
            List<NfeDados> notasRecebidas = sefazClient.consultarNfes(cnpj.getCnpj());
            List<NotaFiscal> notasSalvas = notasRecebidas.stream()
                    .map(dados -> upsertNfe(dados, cnpj, tenant))
                    .toList();

            consulta.setSucesso(true);
            consulta.setStatusHttp(200);
            consulta.setNotasEncontradas(notasSalvas.size());
            consulta.setMensagem("Consulta realizada com sucesso.");
            consulta.setFinalizadoEm(LocalDateTime.now());
            sefazConsultaRepository.save(consulta);

            log.info("[Job] Sincronizadas {} NF-es para CNPJ: {}", notasSalvas.size(), cnpj.getCnpj());
            return notasSalvas; // <-- agora retorna a lista, não o size

        } catch (Exception e) {
            consulta.setSucesso(false);
            consulta.setMensagem("Erro: " + e.getMessage());
            consulta.setFinalizadoEm(LocalDateTime.now());
            sefazConsultaRepository.save(consulta);
            log.error("[Job] Erro na consulta SEFAZ para CNPJ {}: {}", cnpj.getCnpj(), e.getMessage());
            throw new RuntimeException("Falha ao consultar SEFAZ: " + e.getMessage());
        }
    }

    private NotaFiscal upsertNfe(NfeDados dados, Cnpj cnpj, Tenant tenant) {
        return nfeRepository.findByChaveAcesso(dados.chaveAcesso())
                .map(existente -> {
                    existente.setStatus(dados.status());
                    existente.setDataAutorizacao(dados.dataAutorizacao());
                    existente.setValorTotal(dados.valorTotal());
                    return nfeRepository.save(existente);
                })
                .orElseGet(() -> nfeRepository.save(NotaFiscal.builder()
                        .tenant(tenant)
                        .cnpj(cnpj)
                        .chaveAcesso(dados.chaveAcesso())
                        .numero(dados.numero())
                        .serie(dados.serie())
                        .dataEmissao(dados.dataEmissao())
                        .dataAutorizacao(dados.dataAutorizacao())
                        .valorTotal(dados.valorTotal())
                        .status(dados.status())
                        .emitenteCnpj(dados.emitenteCnpj())
                        .emitenteNome(dados.emitenteNome())
                        .destinatarioCnpj(dados.destinatarioCnpj())
                        .destinatarioNome(dados.destinatarioNome())
                        .build()));
    }

    private Cnpj validarCnpjDoTenant(UUID cnpjId, UUID tenantId) {
        return cnpjRepository.findByIdAndTenantId(cnpjId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CNPJ não encontrado"));
    }
}