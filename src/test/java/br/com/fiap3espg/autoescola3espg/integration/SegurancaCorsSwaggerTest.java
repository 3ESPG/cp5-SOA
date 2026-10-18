package br.com.fiap3espg.autoescola3espg.integration;

import br.com.fiap3espg.autoescola3espg.domain.usuario.Role;
import br.com.fiap3espg.autoescola3espg.domain.usuario.Usuario;
import br.com.fiap3espg.autoescola3espg.domain.usuario.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Autenticação JWT, autorização por perfil, CORS e documentação Swagger/OpenAPI. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SegurancaCorsSwaggerTest {
    private static final String ORIGEM_PERMITIDA = "http://localhost:3000";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    private String criarUsuarioEObterToken(Role perfil) throws Exception {
        String login = "teste-" + UUID.randomUUID();
        usuarioRepository.save(new Usuario(login, passwordEncoder.encode("senha123"), perfil));
        String resposta = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenJWT").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.tokenJWT");
    }

    // ---------------- JWT ----------------
    @Test
    @DisplayName("Login com senha errada retorna 401")
    void loginInvalido() throws Exception {
        String login = "teste-" + UUID.randomUUID();
        usuarioRepository.save(new Usuario(login, passwordEncoder.encode("senha123"), Role.USER));
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login sem corpo válido retorna 400")
    void loginSemDados() throws Exception {
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Rota protegida sem token retorna 401")
    void semToken() throws Exception {
        mockMvc.perform(get("/instrutores")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token inválido retorna 401 (e não 500)")
    void tokenInvalido() throws Exception {
        mockMvc.perform(get("/instrutores").header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token válido de ADMIN acessa rotas de ADMIN")
    void tokenAdmin() throws Exception {
        String token = criarUsuarioEObterToken(Role.ADMIN);
        mockMvc.perform(get("/instrutores").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Perfil USER lista instrutores (200) mas não acessa rotas de ADMIN (403)")
    void perfilUser() throws Exception {
        String token = criarUsuarioEObterToken(Role.USER);
        mockMvc.perform(get("/instrutores").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
        // Rota sem corpo: com corpo inválido, o @Valid responderia 400 antes da checagem de perfil
        mockMvc.perform(delete("/alunos/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ---------------- CORS ----------------
    @Test
    @DisplayName("CORS: preflight de origem permitida é aceito com métodos e headers esperados")
    void corsPreflightPermitido() throws Exception {
        mockMvc.perform(options("/instrutores")
                        .header(HttpHeaders.ORIGIN, ORIGEM_PERMITIDA)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM_PERMITIDA))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")));
    }

    @Test
    @DisplayName("CORS: preflight de origem não permitida é rejeitado (403)")
    void corsPreflightNegado() throws Exception {
        mockMvc.perform(options("/instrutores")
                        .header(HttpHeaders.ORIGIN, "http://site-nao-autorizado.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CORS: requisição /login de origem permitida recebe o header Allow-Origin")
    void corsNaRequisicaoReal() throws Exception {
        mockMvc.perform(post("/login").header(HttpHeaders.ORIGIN, ORIGEM_PERMITIDA)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM_PERMITIDA));
    }

    // ---------------- Swagger / OpenAPI ----------------
    @Test
    @DisplayName("OpenAPI: /v3/api-docs é público e documenta os endpoints, o esquema JWT e os códigos HTTP")
    void openApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/alunos")))
                .andExpect(content().string(containsString("/instrutores")))
                .andExpect(content().string(containsString("/usuarios")))
                .andExpect(content().string(containsString("/instrucoes")))
                .andExpect(content().string(containsString("/integracoes/cep/{cep}")))
                .andExpect(content().string(containsString("/login")))
                .andExpect(content().string(containsString("bearer-key")))
                .andExpect(content().string(containsString("\"502\"")));
    }

    @Test
    @DisplayName("Swagger UI: página acessível sem autenticação")
    void swaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
