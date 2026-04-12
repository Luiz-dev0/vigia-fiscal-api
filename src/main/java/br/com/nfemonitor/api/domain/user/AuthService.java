package br.com.nfemonitor.api.domain.user;

import br.com.nfemonitor.api.application.dto.LoginRequest;
import br.com.nfemonitor.api.application.dto.RegistroRequest;
import br.com.nfemonitor.api.application.dto.TokenResponse;
import br.com.nfemonitor.api.domain.tenant.Tenant;
import br.com.nfemonitor.api.domain.tenant.TenantRepository;
import br.com.nfemonitor.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    @Transactional
    public TokenResponse registrar(RegistroRequest req) {
        if (tenantRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name(req.nome())
                .email(req.email())
                .whatsapp(req.whatsapp())
                .trialEndsAt(LocalDateTime.now().plusDays(7))
                .build());

        User user = userRepository.save(User.builder()
                .tenant(tenant)
                .email(req.email())
                .name(req.nome())
                .passwordHash(passwordEncoder.encode(req.senha()))
                .role("ADMIN")
                .build());

        String token = jwtService.gerarToken(user, tenant.getId());
        return TokenResponse.of(token, user.getName(), user.getEmail());
    }

    public TokenResponse entrar(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.senha())
        );

        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        String token = jwtService.gerarToken(user, user.getTenant().getId());
        return TokenResponse.of(token, user.getName(), user.getEmail());
    }
}