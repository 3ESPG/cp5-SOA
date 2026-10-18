package br.com.fiap3espg.autoescola3espg.domain.instrucao;

import br.com.fiap3espg.autoescola3espg.domain.aluno.Aluno;
import br.com.fiap3espg.autoescola3espg.domain.endereco.Endereco;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Especialidade;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Instrutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InstrucaoRepositoryTest {
    @Autowired
    InstrucaoRepository repository;

    @Autowired
    TestEntityManager testEntity;

    private final LocalDateTime dia10h = LocalDateTime.of(2026, 10, 19, 10, 0);

    private Instrucao agendar() {
        Endereco endereco = new Endereco("Rua A", "1", null, "Centro", "São Paulo", "SP", "01001-000");
        Aluno aluno = new Aluno(null, "Aluno", "aluno@email.com", "11999999999", "12345678901", endereco, true);
        Instrutor instrutor = new Instrutor(null, "Instrutor", "instrutor@email.com", "11999999999", "01234567890",
                Especialidade.CARROS, endereco, true);
        testEntity.persist(aluno);
        testEntity.persist(instrutor);
        Instrucao instrucao = new Instrucao(null, aluno, instrutor, dia10h);
        testEntity.persistAndFlush(instrucao);
        return instrucao;
    }

    @Test
    @DisplayName("Detecta conflito de horário do instrutor")
    void conflitoInstrutor() {
        Instrucao instrucao = agendar();
        Long idInstrutor = instrucao.getInstrutor().getId();
        assertThat(repository.existsByInstrutorIdAndDataHora(idInstrutor, dia10h)).isTrue();
        assertThat(repository.existsByInstrutorIdAndDataHora(idInstrutor, dia10h.plusHours(1))).isFalse();
    }

    @Test
    @DisplayName("Detecta instrução do aluno no mesmo dia (intervalo 06:00-20:00)")
    void limiteDiarioAluno() {
        Instrucao instrucao = agendar();
        Long idAluno = instrucao.getAluno().getId();
        assertThat(repository.existsByAlunoIdAndDataHoraBetween(idAluno, dia10h.withHour(6), dia10h.withHour(20))).isTrue();
        assertThat(repository.existsByAlunoIdAndDataHoraBetween(idAluno,
                dia10h.plusDays(1).withHour(6), dia10h.plusDays(1).withHour(20))).isFalse();
    }

    @Test
    @DisplayName("Cancelar (delete) libera o horário")
    void cancelarLiberaHorario() {
        Instrucao instrucao = agendar();
        Long idInstrutor = instrucao.getInstrutor().getId();
        repository.delete(instrucao);
        repository.flush();
        assertThat(repository.existsByInstrutorIdAndDataHora(idInstrutor, dia10h)).isFalse();
    }
}
