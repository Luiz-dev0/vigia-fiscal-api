package br.com.nfemonitor.api.domain.manifestacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ManifestacaoRepository extends JpaRepository<ManifestacaoDestinatario, UUID> {

    List<ManifestacaoDestinatario> findAllByNotaFiscalIdAndTenantId(UUID notaFiscalId, UUID tenantId);

    boolean existsByNotaFiscalIdAndTipoEvento(UUID notaFiscalId, TipoManifestacao tipo);

    boolean existsByNotaFiscalIdAndTipoEventoIn(UUID notaFiscalId, List<TipoManifestacao> tipos);
}