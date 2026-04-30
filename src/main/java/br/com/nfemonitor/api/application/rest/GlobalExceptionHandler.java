package br.com.nfemonitor.api.application.rest;

import br.com.nfemonitor.api.domain.certificado.CertificadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CertificadoException.SenhaInvalidaException.class)
    public ResponseEntity<Map<String, String>> handleSenhaInvalida(CertificadoException.SenhaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(CertificadoException.CertificadoVencidoException.class)
    public ResponseEntity<Map<String, String>> handleCertificadoVencido(CertificadoException.CertificadoVencidoException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(CertificadoException.CertificadoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleCertificadoNaoEncontrado(CertificadoException.CertificadoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", ex.getMessage()));
    }
}