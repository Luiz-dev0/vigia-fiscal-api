package br.com.nfemonitor.api.application.rest;

import br.com.nfemonitor.api.infrastructure.sefaz.NfeMonitorJob;
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

    private final NfeMonitorJob nfeMonitorJob;

    @PostMapping("/executar")
    public ResponseEntity<String> executarJob() {
        log.info("[AdminController] Execução manual do job solicitada");
        nfeMonitorJob.executar();
        return ResponseEntity.ok("Job executado com sucesso");
    }
}