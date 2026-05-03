package br.com.nfemonitor.api.security;

import br.com.nfemonitor.api.domain.user.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                String email = jwtService.extrairEmail(token);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userOpt = userRepository.findByEmail(email);
                    if (userOpt.isPresent()) {
                        var user = userOpt.get();
                        if (jwtService.tokenValido(token, user)) {
                            var auth = new UsernamePasswordAuthenticationToken(
                                    user, null, user.getAuthorities());
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            TenantContext.set(jwtService.extrairTenantId(token));
                        }
                    }
                }
            }
            chain.doFilter(req, res);
        } catch (ExpiredJwtException e) {
            TenantContext.clear();
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"message\":\"Token expirado. Faça login novamente.\"}");
        } finally {
            TenantContext.clear();
        }
    }
}