package br.com.nfemonitor.api.infrastructure.sefaz;

import br.com.nfemonitor.api.domain.cnpj.Cnpj;
import br.com.nfemonitor.api.domain.sefaz.SefazConsulta;
import br.com.nfemonitor.api.domain.sefaz.SefazConsultaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SefazConsultationService {

    private final SefazConsultaRepository sefazConsultaRepository;

    public void registrarSucesso(Cnpj cnpj, int total) {
        SefazConsulta consulta = SefazConsulta.builder()
                .tenant(cnpj.getTenant())
                .cnpj(cnpj)
                .tipo("CONSULTA_DISTRIBUICAO_DFE")
                .sucesso(true)
                .statusHttp(200)
                .notasEncontradas(total)
                .mensagem("Consulta realizada com sucesso.")
                .finalizadoEm(LocalDateTime.now())
                .build();

        sefazConsultaRepository.save(consulta);
        log.info("[SefazConsultation] CNPJ {} — {} NF-e(s) sincronizada(s)", cnpj.getCnpj(), total);
    }

    public void registrarErro(Cnpj cnpj, String erro) {
        SefazConsulta consulta = SefazConsulta.builder()
                .tenant(cnpj.getTenant())
                .cnpj(cnpj)
                .tipo("CONSULTA_DISTRIBUICAO_DFE")
                .sucesso(false)
                .statusHttp(500)
                .notasEncontradas(0)
                .mensagem("Erro: " + erro)
                .finalizadoEm(LocalDateTime.now())
                .build();

        sefazConsultaRepository.save(consulta);
        log.error("[SefazConsultation] CNPJ {} — Erro: {}", cnpj.getCnpj(), erro);
    }
}