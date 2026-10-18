package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosDetalhamentoAgendamento;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.InstrucaoNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Especialidade;
import br.com.fiap3espg.autoescola3espg.service.InstrucaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class InstrucaoControllerTest {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    InstrucaoService service;

    private String corpoValido(LocalDateTime dataHora) {
        return """
                {"id_aluno": 1, "id_instrutor": 2, "especialidade": "CARROS", "data_hora": "%s"}
                """.formatted(dataHora.format(FORMATO));
    }

    @Test
    @DisplayName("Expectativa: retornar código 400 para informações inválidas")
    @WithMockUser
    void agendarInstrucaoCenario1() throws Exception {
        mockMvc.perform(post("/instrucoes"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Agendar: 200 com os dados da instrução")
    void agendarSucesso() throws Exception {
        LocalDateTime data = LocalDateTime.now().plusDays(3).withMinute(0).withSecond(0).withNano(0);
        when(service.agendarInstrucao(any())).thenReturn(
                new DadosDetalhamentoAgendamento(10L, "Maria", "Carlos", Especialidade.CARROS, data));

        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON).content(corpoValido(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nomeAluno").value("Maria"))
                .andExpect(jsonPath("$.nomeInstrutor").value("Carlos"));
    }

    @Test
    @WithMockUser
    @DisplayName("Agendar: data no passado é rejeitada com 400")
    void agendarDataPassada() throws Exception {
        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoValido(LocalDateTime.now().minusDays(2))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Agendar: regra de negócio violada retorna 400 com mensagem")
    void agendarRegraViolada() throws Exception {
        when(service.agendarInstrucao(any())).thenThrow(new ValidacaoException("Instrutor ocupado na data / hora escolhida!"));
        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoValido(LocalDateTime.now().plusDays(3))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Instrutor ocupado na data / hora escolhida!"));
    }

    @Test
    @WithMockUser
    @DisplayName("Agendar: aluno inexistente retorna 404")
    void agendarAlunoInexistente() throws Exception {
        when(service.agendarInstrucao(any())).thenThrow(new AlunoNotFoundException("ID do aluno informado não existe!"));
        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoValido(LocalDateTime.now().plusDays(3))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("Cancelar: 204 quando a instrução existe")
    void cancelarSucesso() throws Exception {
        mockMvc.perform(delete("/instrucoes/10")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("Cancelar: 404 quando a instrução não existe")
    void cancelarInexistente() throws Exception {
        doThrow(new InstrucaoNotFoundException("ID da instrução informado não existe!")).when(service).cancelarInstrucao(99L);
        mockMvc.perform(delete("/instrucoes/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Sem autenticação: 401")
    void semAutenticacao() throws Exception {
        mockMvc.perform(delete("/instrucoes/10")).andExpect(status().isUnauthorized());
    }
}
