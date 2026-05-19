package br.com.vigiafiscal.api.application.rest;

import br.com.vigiafiscal.api.application.dto.LoginRequest;
import br.com.vigiafiscal.api.application.dto.RegistroRequest;
import br.com.vigiafiscal.api.application.dto.TokenResponse;
import br.com.vigiafiscal.api.domain.user.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registrar")
    public ResponseEntity<TokenResponse> registrar(@Valid @RequestBody RegistroRequest req) {
        return ResponseEntity.ok(authService.registrar(req));
    }

    @PostMapping("/entrar")
    public ResponseEntity<TokenResponse> entrar(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.entrar(req));
    }
}
