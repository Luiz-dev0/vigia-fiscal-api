package br.com.nfemonitor.api.application.rest;

import br.com.nfemonitor.api.application.dto.CertificadoResponse;
import br.com.nfemonitor.api.domain.certificado.CertificadoA1;
import br.com.nfemonitor.api.domain.certificado.CertificadoException;
import br.com.nfemonitor.api.domain.certificado.CertificadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/certificados")
@RequiredArgsConstructor
public class CertificadoController {

    private final CertificadoService certificadoService;

    /**
     * Upload do certificado A1.
     * Recebe o arquivo .pfx e a senha via multipart/form-data.
     * A senha não é persistida.
     */
    @PostMapping(value = "/{cnpjId}", consumes = "multipart/form-data")
    public ResponseEntity<CertificadoResponse> upload(
            @PathVariable UUID cnpjId,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("senha") String senha
    ) throws IOException {
        byte[] pfxBytes = arquivo.getBytes();
        CertificadoA1 certificado = certificadoService.salvar(cnpjId, pfxBytes, senha);
        return ResponseEntity.ok(CertificadoResponse.from(certificado));
    }

    /**
     * Retorna informações do certificado (validade e status).
     * Nunca retorna o pfxData.
     */
    @GetMapping("/{cnpjId}")
    public ResponseEntity<CertificadoResponse> buscar(@PathVariable UUID cnpjId) {
        try {
            CertificadoA1 certificado = certificadoService.buscarInfo(cnpjId);
            return ResponseEntity.ok(CertificadoResponse.from(certificado));
        } catch (CertificadoException.CertificadoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove o certificado vinculado ao CNPJ.
     */
    @DeleteMapping("/{cnpjId}")
    public ResponseEntity<Void> remover(@PathVariable UUID cnpjId) {
        certificadoService.remover(cnpjId);
        return ResponseEntity.noContent().build();
    }
}