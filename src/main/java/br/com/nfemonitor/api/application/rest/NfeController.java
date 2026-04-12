package br.com.nfemonitor.api.application.rest;

import br.com.nfemonitor.api.application.dto.NfeResponse;
import br.com.nfemonitor.api.domain.nfe.NfeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/nfes")
@RequiredArgsConstructor
public class NfeController {

    private final NfeService nfeService;

    @GetMapping
    public ResponseEntity<List<NfeResponse>> listar(
            @RequestParam(required = false) UUID cnpjId) {
        if (cnpjId != null) {
            return ResponseEntity.ok(nfeService.listarPorCnpj(cnpjId));
        }
        return ResponseEntity.ok(nfeService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NfeResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(nfeService.buscarPorId(id));
    }

    @PostMapping("/sincronizar/{cnpjId}")
    public ResponseEntity<List<NfeResponse>> sincronizar(@PathVariable UUID cnpjId) {
        return ResponseEntity.ok(nfeService.sincronizarNfesDoCnpj(cnpjId));
    }
}