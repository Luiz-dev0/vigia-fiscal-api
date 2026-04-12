package br.com.nfemonitor.api.domain.sefaz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SefazConsultaRepository extends JpaRepository<SefazConsulta, UUID> {

    List<SefazConsulta> findTop10ByCnpjIdOrderByIniciadoEmDesc(UUID cnpjId);
}