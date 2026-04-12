package br.com.nfemonitor.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NF-e Monitor API")
                        .description("""
                                API de monitoramento de Notas Fiscais Eletrônicas para contadores e pequenas empresas.
                                
                                **Como autenticar:**
                                1. Faça `POST /auth/login` para obter seu token JWT
                                2. Clique em **Authorize** (canto superior direito)
                                3. Cole o token no campo — **sem** o prefixo `Bearer`
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("NF-e Monitor")
                                .email("suporte@nfemonitor.com.br"))
                        .license(new License()
                                .name("Proprietário")))
                // Aplica o esquema JWT globalmente a todos os endpoints
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtido em POST /auth/login")));
    }
}