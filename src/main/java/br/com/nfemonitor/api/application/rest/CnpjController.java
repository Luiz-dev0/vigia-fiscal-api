package br.com.nfemonitor.api.application.rest;

import br.com.nfemonitor.api.application.dto.CnpjRequest;
import br.com.nfemonitor.api.application.dto.CnpjResponse;
import br.com.nfemonitor.api.domain.cnpj.CnpjService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cnpjs")
@RequiredArgsConstructor
public class CnpjController {

    private final CnpjService cnpjService;

    @GetMapping
    public ResponseEntity<List<CnpjResponse>> listar() {
        return ResponseEntity.ok(cnpjService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CnpjResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(cnpjService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<CnpjResponse> cadastrar(@Valid @RequestBody CnpjRequest req) {
        return ResponseEntity.ok(cnpjService.cadastrar(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        cnpjService.remover(id);
        return ResponseEntity.noContent().build();
    }
}