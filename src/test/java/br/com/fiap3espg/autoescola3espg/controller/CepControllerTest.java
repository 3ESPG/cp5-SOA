package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.integration.cep.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Endpoint de consulta de CEP, com o serviço da API externa simulado (sem rede). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CepControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CepService cepService;

    @Test
    @WithMockUser
    @DisplayName("200: devolve o endereço em JSON")
    void sucesso() throws Exception {
        when(cepService.consultar("01001000")).thenReturn(
                new ConsultaCepResponse("01001-000", "Praça da Sé", "", "Sé", "São Paulo", "SP", null));

        mockMvc.perform(get("/integracoes/cep/01001000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001-000"))
                .andExpect(jsonPath("$.logradouro").value("Praça da Sé"))
                .andExpect(jsonPath("$.localidade").value("São Paulo"))
                .andExpect(jsonPath("$.uf").value("SP"));
    }

    @Test
    @WithMockUser
    @DisplayName("400: CEP inválido")
    void cepInvalido() throws Exception {
        when(cepService.consultar("123")).thenThrow(new CepInvalidoException("CEP inválido"));
        mockMvc.perform(get("/integracoes/cep/123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CEP inválido"));
    }

    @Test
    @WithMockUser
    @DisplayName("404: CEP inexistente")
    void cepInexistente() throws Exception {
        when(cepService.consultar("99999999")).thenThrow(new CepNaoEncontradoException("CEP não encontrado"));
        mockMvc.perform(get("/integracoes/cep/99999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("502: ViaCEP indisponível")
    void servicoIndisponivel() throws Exception {
        when(cepService.consultar("01001000")).thenThrow(new CepIndisponivelException("ViaCEP fora do ar", new RuntimeException()));
        mockMvc.perform(get("/integracoes/cep/01001000"))
                .andExpect(status().isBadGateway());
    }

    @Test
    @DisplayName("401: sem token JWT")
    void semAutenticacao() throws Exception {
        mockMvc.perform(get("/integracoes/cep/01001000"))
                .andExpect(status().isUnauthorized());
    }
}
