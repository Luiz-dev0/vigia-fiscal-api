package br.com.nfemonitor.api.integration;

import br.com.nfemonitor.api.infrastructure.notification.EmailSender;
import br.com.nfemonitor.api.infrastructure.notification.WhatsAppSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("src/test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Fluxo principal — registro, login, CNPJ e regra de alerta")
class FluxoPrincipalIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private WhatsAppSender whatsAppSender;
    @MockitoBean private EmailSender emailSender;

    private static String tokenJwt;
    private static String cnpjId;

    @Test
    @Order(1)
    @DisplayName("1. Deve registrar novo usuário e retornar token")
    void deveRegistrarNovoUsuario() throws Exception {
        String payload = """
            {
                "nome": "Contador Integração",
                "email": "integracao@nfemonitor.com.br",
                "senha": "Senha@2024",
                "whatsapp": "+5565999999999"
            }
            """;

        MvcResult resultado = mockMvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        String body = resultado.getResponse().getContentAsString();
        tokenJwt = objectMapper.readTree(body).get("token").asText();
        assertThat(tokenJwt).isNotBlank();
    }

    @Test
    @Order(2)
    @DisplayName("2. Deve autenticar e retornar JWT válido")
    void deveAutenticarERetornarJwt() throws Exception {
        String payload = """
            {
                "email": "integracao@nfemonitor.com.br",
                "senha": "Senha@2024"
            }
            """;

        MvcResult resultado = mockMvc.perform(post("/auth/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        String body = resultado.getResponse().getContentAsString();
        tokenJwt = objectMapper.readTree(body).get("token").asText();
        assertThat(tokenJwt).isNotBlank();
    }

    @Test
    @Order(3)
    @DisplayName("3. Endpoint protegido deve retornar 403 sem token")
    void endpointProtegidoExigeToken() throws Exception {
        // Spring Security retorna 403 (Forbidden) por padrão quando não há credenciais
        mockMvc.perform(get("/cnpjs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    @DisplayName("4. Deve cadastrar CNPJ com token válido")
    void deveCadastrarCnpj() throws Exception {
        String payload = """
            {
                "cnpj": "12345678000195",
                "razaoSocial": "Empresa Cliente Teste SA",
                "uf": "MT"
            }
            """;

        MvcResult resultado = mockMvc.perform(post("/cnpjs")
                        .header("Authorization", "Bearer " + tokenJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        String body = resultado.getResponse().getContentAsString();
        cnpjId = objectMapper.readTree(body).get("id").asText();
        assertThat(cnpjId).isNotBlank();
    }

    @Test
    @Order(5)
    @DisplayName("5. Deve criar regra de alerta para NOTA_REJEITADA via EMAIL")
    void deveCriarRegraDeAlerta() throws Exception {
        String payload = String.format("""
            {
                "cnpjId": "%s",
                "eventType": "NOTA_REJEITADA",
                "channel": "EMAIL",
                "destination": "contador@teste.com.br",
                "minutesBefore": 0
            }
            """, cnpjId);

        mockMvc.perform(post("/alertas")
                        .header("Authorization", "Bearer " + tokenJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventType").value("NOTA_REJEITADA"))
                .andExpect(jsonPath("$.channel").value("EMAIL"));
    }

    @Test
    @Order(6)
    @DisplayName("6. Deve listar CNPJs do tenant autenticado")
    void deveListarCnpjs() throws Exception {
        mockMvc.perform(get("/cnpjs")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cnpj").value("12345678000195"));
    }
}