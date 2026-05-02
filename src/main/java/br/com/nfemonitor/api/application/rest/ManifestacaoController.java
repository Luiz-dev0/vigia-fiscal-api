package br.com.nfemonitor.api.application.rest;

import br.com.nfemonitor.api.application.dto.ManifestacaoRequest;
import br.com.nfemonitor.api.application.dto.ManifestacaoResponse;
import br.com.nfemonitor.api.domain.manifestacao.ManifestacaoService;
import br.com.nfemonitor.api.domain.manifestacao.TipoManifestacao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/manifestacoes")
@RequiredArgsConstructor
public class ManifestacaoController {

    private final ManifestacaoService manifestacaoService;

    @PostMapping("/{nfeId}")
    public ResponseEntity<ManifestacaoResponse> manifestar(
            @PathVariable UUID nfeId,
            @RequestBody ManifestacaoRequest request
    ) {
        TipoManifestacao tipo = TipoManifestacao.valueOf(request.getTipoEvento());
        var manifestacao = manifestacaoService.manifestar(nfeId, tipo, request.getSenha());
        return ResponseEntity.ok(ManifestacaoResponse.from(manifestacao));
    }

    @GetMapping("/{nfeId}")
    public ResponseEntity<List<ManifestacaoResponse>> listar(@PathVariable UUID nfeId) {
        var lista = manifestacaoService.listarPorNfe(nfeId)
                .stream()
                .map(ManifestacaoResponse::from)
                .toList();
        return ResponseEntity.ok(lista);
    }
}