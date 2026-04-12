package br.com.nfemonitor.api.domain.cnpj;

import br.com.nfemonitor.api.application.dto.CnpjRequest;
import br.com.nfemonitor.api.application.dto.CnpjResponse;
import br.com.nfemonitor.api.domain.tenant.Tenant;
import br.com.nfemonitor.api.domain.tenant.TenantRepository;
import br.com.nfemonitor.api.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CnpjService {

    private final CnpjRepository cnpjRepository;
    private final TenantRepository tenantRepository;

    private static final int LIMITE_TRIAL = 1;
    private static final int LIMITE_BASICO = 1;
    private static final int LIMITE_PRO = 5;

    public List<CnpjResponse> listar() {
        UUID tenantId = TenantContext.get();
        return cnpjRepository.findByTenantIdAndActiveTrue(tenantId)
                .stream().map(CnpjResponse::from).toList();
    }

    public CnpjResponse buscar(UUID id) {
        UUID tenantId = TenantContext.get();
        return CnpjResponse.from(cnpjRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("CNPJ não encontrado")));
    }

    @Transactional
    public CnpjResponse cadastrar(CnpjRequest req) {
        UUID tenantId = TenantContext.get();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant não encontrado"));

        validarLimite(tenant);

        String cnpjLimpo = req.cnpj().replaceAll("[^\\d]", "");

        if (cnpjRepository.existsByTenantIdAndCnpj(tenantId, cnpjLimpo)) {
            throw new IllegalArgumentException("CNPJ já cadastrado nesta conta");
        }

        Cnpj cnpj = cnpjRepository.save(Cnpj.builder()
                .tenant(tenant)
                .cnpj(cnpjLimpo)
                .razaoSocial(req.razaoSocial())
                .nomeFantasia(req.nomeFantasia())
                .ie(req.ie())
                .uf(req.uf().toUpperCase())
                .emailContato(req.emailContato())
                .build());

        return CnpjResponse.from(cnpj);
    }

    @Transactional
    public void remover(UUID id) {
        UUID tenantId = TenantContext.get();
        Cnpj cnpj = cnpjRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("CNPJ não encontrado"));
        cnpj.setActive(false); // soft delete
        cnpjRepository.save(cnpj);
    }

    private void validarLimite(Tenant tenant) {
        long total = cnpjRepository.countByTenantIdAndActiveTrue(tenant.getId());
        int limite = switch (tenant.getPlan()) {
            case "BASICO" -> LIMITE_BASICO;
            case "PRO"    -> LIMITE_PRO;
            case "TRIAL"  -> LIMITE_TRIAL;
            default       -> Integer.MAX_VALUE; // ENTERPRISE
        };
        if (total >= limite) {
            throw new IllegalStateException(
                    "Limite de CNPJs atingido para o plano " + tenant.getPlan() + ". Faça upgrade.");
        }
    }
}