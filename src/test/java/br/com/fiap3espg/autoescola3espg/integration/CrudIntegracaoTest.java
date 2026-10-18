package br.com.fiap3espg.autoescola3espg.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração ponta a ponta (controller -> service -> JPA -> H2) dos CRUDs e do
 * agendamento/cancelamento. Cada teste é revertido ao final (@Transactional).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class CrudIntegracaoTest {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
    private static final AtomicLong SEQ = new AtomicLong(10_000_000_000L);

    @Autowired
    MockMvc mockMvc;

    private static final String ENDERECO = """
            {"logradouro":"Rua A","numero":"10","complemento":"","bairro":"Centro","cidade":"São Paulo","uf":"SP","cep":"01001-000"}
            """;

    private long criarAluno() throws Exception {
        long n = SEQ.incrementAndGet();
        String corpo = """
                {"nome":"Aluno %d","email":"aluno%d@email.com","telefone":"11999999999","cpf":"%011d","endereco":%s}
                """.formatted(n, n, n, ENDERECO);
        String resposta = mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private long criarInstrutor(String especialidade) throws Exception {
        long n = SEQ.incrementAndGet();
        String corpo = """
                {"nome":"Instrutor %d","email":"instrutor%d@email.com","telefone":"11988888888","cnh":"%011d","especialidade":"%s","endereco":%s}
                """.formatted(n, n, n, especialidade, ENDERECO);
        String resposta = mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private String proximaSegundaAs(int hora) {
        return LocalDateTime.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(hora).withMinute(0).withSecond(0).withNano(0).format(FORMATO);
    }

    private String corpoAgendamento(long alunoId, Long instrutorId, String especialidade, String dataHora) {
        String instrutor = instrutorId == null ? "" : "\"id_instrutor\": " + instrutorId + ",";
        String esp = especialidade == null ? "" : "\"especialidade\": \"" + especialidade + "\",";
        return "{\"id_aluno\": " + alunoId + "," + instrutor + esp + "\"data_hora\": \"" + dataHora + "\"}";
    }

    // ---------------- ALUNO ----------------
    @Test
    @DisplayName("Aluno: cadastrar, detalhar, atualizar, listar e excluir (lógico)")
    void crudAluno() throws Exception {
        long id = criarAluno();

        mockMvc.perform(get("/alunos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.endereco.cidade").value("São Paulo"));

        mockMvc.perform(put("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": " + id + ", \"nome\": \"Nome Atualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"));

        mockMvc.perform(get("/alunos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == %d)]".formatted(id)).exists());

        mockMvc.perform(delete("/alunos/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/alunos/{id}", id)).andExpect(jsonPath("$.ativo").value(false));
        mockMvc.perform(get("/alunos"))
                .andExpect(jsonPath("$.content[?(@.id == %d)]".formatted(id)).doesNotExist());
    }

    @Test
    @DisplayName("Aluno: dados inválidos retornam 400 e inexistente retorna 404")
    void alunoErros() throws Exception {
        mockMvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"x\",\"telefone\":\"1\",\"cpf\":\"123\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/alunos/{id}", 987654321L)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/alunos/{id}", 987654321L)).andExpect(status().isNotFound());
    }

    // ---------------- INSTRUTOR ----------------
    @Test
    @DisplayName("Instrutor: cadastrar, detalhar, atualizar, listar e excluir (lógico)")
    void crudInstrutor() throws Exception {
        long id = criarInstrutor("MOTOS");

        mockMvc.perform(get("/instrutores/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especialidade").value("MOTOS"));

        mockMvc.perform(put("/instrutores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": " + id + ", \"especialidade\": \"VANS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especialidade").value("VANS"));

        mockMvc.perform(get("/instrutores"))
                .andExpect(jsonPath("$.content[?(@.id == %d)]".formatted(id)).exists());

        mockMvc.perform(delete("/instrutores/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/instrutores"))
                .andExpect(jsonPath("$.content[?(@.id == %d)]".formatted(id)).doesNotExist());
    }

    @Test
    @DisplayName("Instrutor: sem endereço retorna 400 e inexistente retorna 404")
    void instrutorErros() throws Exception {
        mockMvc.perform(post("/instrutores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"email\":\"x@email.com\",\"telefone\":\"1\",\"cnh\":\"01234567890\",\"especialidade\":\"MOTOS\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/instrutores/{id}", 987654321L)).andExpect(status().isNotFound());
    }

    // ---------------- USUÁRIO ----------------
    @Test
    @DisplayName("Usuário: cadastrar (sem expor senha), detalhar, atualizar, listar e excluir")
    void crudUsuario() throws Exception {
        String login = "user-" + UUID.randomUUID();
        String resposta = mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\",\"senha\":\"senha123\",\"perfil\":\"USER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(content().string(not(containsString("senha123"))))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(resposta, "$.id")).longValue();

        mockMvc.perform(get("/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("USER"));

        mockMvc.perform(put("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": " + id + ", \"perfil\": \"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ADMIN"));

        mockMvc.perform(get("/usuarios")).andExpect(status().isOk());

        mockMvc.perform(delete("/usuarios/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/usuarios/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Usuário: login duplicado e senha curta retornam 400")
    void usuarioErros() throws Exception {
        String login = "dup-" + UUID.randomUUID();
        String corpo = "{\"login\":\"" + login + "\",\"senha\":\"senha123\",\"perfil\":\"USER\"}";
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"outro\",\"senha\":\"123\",\"perfil\":\"USER\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- INSTRUÇÃO ----------------
    @Test
    @DisplayName("Instrução: agendar, detalhar, listar, impedir conflito, cancelar e reagendar")
    void agendarEcancelarInstrucao() throws Exception {
        long aluno1 = criarAluno();
        long aluno2 = criarAluno();
        long instrutor = criarInstrutor("CARROS");
        String dataHora = proximaSegundaAs(10);

        String resposta = mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(aluno1, instrutor, null, dataHora)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataHora").value(dataHora))
                .andReturn().getResponse().getContentAsString();
        long idInstrucao = ((Number) JsonPath.read(resposta, "$.id")).longValue();

        mockMvc.perform(get("/instrucoes/{id}", idInstrucao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeInstrutor").exists());
        mockMvc.perform(get("/instrucoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == %d)]".formatted(idInstrucao)).exists());

        // outro aluno, mesmo instrutor e horário -> conflito
        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(aluno2, instrutor, null, dataHora)))
                .andExpect(status().isBadRequest());

        // cancelamento
        mockMvc.perform(delete("/instrucoes/{id}", idInstrucao)).andExpect(status().isNoContent());
        mockMvc.perform(get("/instrucoes/{id}", idInstrucao)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/instrucoes/{id}", idInstrucao)).andExpect(status().isNotFound());

        // horário liberado após o cancelamento
        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(aluno2, instrutor, null, dataHora)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Instrução: sem instrutor informado, escolhe um disponível da especialidade")
    void agendarEscolhendoInstrutorPorEspecialidade() throws Exception {
        long aluno = criarAluno();
        criarInstrutor("CAMINHOES");

        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(aluno, null, "CAMINHOES", proximaSegundaAs(11))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especialidade").value("CAMINHOES"));
    }

    @Test
    @DisplayName("Instrução: sem instrutor disponível retorna 400")
    void agendarSemInstrutorDisponivel() throws Exception {
        long aluno = criarAluno();
        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(aluno, null, "VANS", proximaSegundaAs(12))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Instrução: horário fora da hora inteira, aluno e instrutor inexistentes")
    void agendarErros() throws Exception {
        long aluno = criarAluno();
        long instrutor = criarInstrutor("CARROS");
        String meiaHora = LocalDateTime.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(10).withMinute(30).withSecond(0).withNano(0).format(FORMATO);

        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(aluno, instrutor, null, meiaHora)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(987654321L, instrutor, null, proximaSegundaAs(10))))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(aluno, 987654321L, null, proximaSegundaAs(10))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Instrução: aluno inativo não pode agendar")
    void agendarAlunoInativo() throws Exception {
        long aluno = criarAluno();
        long instrutor = criarInstrutor("CARROS");
        mockMvc.perform(delete("/alunos/{id}", aluno)).andExpect(status().isNoContent());

        mockMvc.perform(post("/instrucoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(aluno, instrutor, null, proximaSegundaAs(10))))
                .andExpect(status().isBadRequest());
    }
}
