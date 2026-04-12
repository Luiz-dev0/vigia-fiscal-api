package br.com.nfemonitor.api.application.rest;

import br.com.nfemonitor.api.application.dto.AlertRuleRequest;
import br.com.nfemonitor.api.application.dto.AlertRuleResponse;
import br.com.nfemonitor.api.domain.alert.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/alertas")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<AlertRuleResponse>> listar() {
        return ResponseEntity.ok(alertService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertRuleResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(alertService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<AlertRuleResponse> criar(@RequestBody @Valid AlertRuleRequest request) {
        AlertRuleResponse response = alertService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        alertService.remover(id);
        return ResponseEntity.noContent().build();
    }
}