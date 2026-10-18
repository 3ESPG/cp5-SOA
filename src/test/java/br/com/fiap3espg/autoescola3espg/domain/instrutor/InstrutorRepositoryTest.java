package br.com.fiap3espg.autoescola3espg.domain.instrutor;

import br.com.fiap3espg.autoescola3espg.domain.aluno.Aluno;
import br.com.fiap3espg.autoescola3espg.domain.endereco.Endereco;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.Instrucao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;

/** Executa contra H2 em memória (perfil "test"); não exige MySQL. */
@DataJpaTest
@ActiveProfiles("test")
public class InstrutorRepositoryTest {
    @Autowired
    InstrutorRepository repository;

    @Autowired
    TestEntityManager testEntity;

    private LocalDateTime proximaSegundaAs10() {
        return LocalDateTime.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    @DisplayName("Expectativa: retornar null quando instrutor cadastrado não está disponível")
    void escolherInstrutorAleatorioDisponivelCenario1() {
        LocalDateTime horario = proximaSegundaAs10();
        Aluno aluno = cadastrarAluno();
        Instrutor instrutor = cadastrarInstrutor("instrutorteste@email.com", "01234567890", Especialidade.MOTOS, true);
        testEntity.persistAndFlush(new Instrucao(null, aluno, instrutor, horario));

        Instrutor disponivel = repository.escolherInstrutorAleatorioDisponivel(Especialidade.MOTOS, horario);

        assertThat(disponivel).isNull();
    }

    @Test
    @DisplayName("Expectativa: retornar instrutor quando instrutor cadastrado está disponível")
    void escolherInstrutorAleatorioDisponivelCenario2() {
        Instrutor instrutor = cadastrarInstrutor("instrutorteste@email.com", "01234567890", Especialidade.MOTOS, true);

        Instrutor disponivel = repository.escolherInstrutorAleatorioDisponivel(Especialidade.MOTOS, proximaSegundaAs10());

        assertThat(disponivel).isEqualTo(instrutor);
    }

    @Test
    @DisplayName("Expectativa: ignorar instrutor inativo e de outra especialidade")
    void escolherInstrutorAleatorioDisponivelCenario3() {
        cadastrarInstrutor("inativo@email.com", "11111111111", Especialidade.MOTOS, false);
        cadastrarInstrutor("carros@email.com", "22222222222", Especialidade.CARROS, true);

        Instrutor disponivel = repository.escolherInstrutorAleatorioDisponivel(Especialidade.MOTOS, proximaSegundaAs10());

        assertThat(disponivel).isNull();
    }

    @Test
    @DisplayName("Expectativa: listagem retorna somente instrutores ativos")
    void findAllByAtivoTrue() {
        Instrutor ativo = cadastrarInstrutor("ativo@email.com", "33333333333", Especialidade.VANS, true);
        cadastrarInstrutor("inativo@email.com", "44444444444", Especialidade.VANS, false);

        var pagina = repository.findAllByAtivoTrue(PageRequest.of(0, 10));

        assertThat(pagina.getContent()).containsExactly(ativo);
    }

    @Test
    @DisplayName("Expectativa: existsByIdAndAtivoFalse identifica instrutor inativo")
    void existsByIdAndAtivoFalse() {
        Instrutor inativo = cadastrarInstrutor("inativo@email.com", "55555555555", Especialidade.CAMINHOES, false);
        Instrutor ativo = cadastrarInstrutor("ativo@email.com", "66666666666", Especialidade.CAMINHOES, true);

        assertThat(repository.existsByIdAndAtivoFalse(inativo.getId())).isTrue();
        assertThat(repository.existsByIdAndAtivoFalse(ativo.getId())).isFalse();
    }

    private Endereco endereco() {
        return new Endereco("Rua Teste", "000", "Casa dos Fundos", "Vila Teste", "Test City", "TS", "01234-000");
    }

    private Aluno cadastrarAluno() {
        Aluno aluno = new Aluno(null, "Aluno Teste", "alunoteste@email.com.br", "(11) 98765-4321", "12345678901", endereco(), true);
        testEntity.persist(aluno);
        return aluno;
    }

    private Instrutor cadastrarInstrutor(String email, String cnh, Especialidade especialidade, boolean ativo) {
        Instrutor instrutor = new Instrutor(null, "Instrutor Teste", email, "(11) 91234-5678", cnh, especialidade, endereco(), ativo);
        testEntity.persistAndFlush(instrutor);
        return instrutor;
    }
}
