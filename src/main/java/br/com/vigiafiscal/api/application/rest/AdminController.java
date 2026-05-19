package br.com.vigiafiscal.api.application.rest;

import br.com.vigiafiscal.api.infrastructure.sefaz.NfeVigiaJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/monitor")
@RequiredArgsConstructor
public class AdminController {

    private final NfeVigiaJob nfeVigiaJob;

    @PostMapping("/executar")
    public ResponseEntity<String> executarJob() {
        log.info("[AdminController] Execução manual do job solicitada");
        nfeVigiaJob.executar();
        return ResponseEntity.ok("Job executado com sucesso");
    }
}
