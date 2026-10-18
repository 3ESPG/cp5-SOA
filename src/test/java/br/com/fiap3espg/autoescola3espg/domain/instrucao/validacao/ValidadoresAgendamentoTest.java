package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoRepository;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.InstrucaoRepository;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Especialidade;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.InstrutorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Regras de negócio do agendamento (validadores já existentes dos checkpoints anteriores). */
@ExtendWith(MockitoExtension.class)
class ValidadoresAgendamentoTest {
    @Mock
    InstrucaoRepository instrucaoRepository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    InstrutorRepository instrutorRepository;

    // 19/10/2026 é segunda-feira; 18/10/2026 é domingo.
    private DadosAgendamentoInstrucao em(LocalDateTime dataHora) {
        return new DadosAgendamentoInstrucao(1L, 2L, Especialidade.CARROS, dataHora);
    }

    @Test
    @DisplayName("Hora inteira: aceita 10:00 e rejeita 10:30")
    void horaInteira() {
        var validador = new ValidadorHoraInteira();
        assertDoesNotThrow(() -> validador.validar(em(LocalDateTime.of(2026, 10, 19, 10, 0))));
        assertThrows(ValidacaoException.class, () -> validador.validar(em(LocalDateTime.of(2026, 10, 19, 10, 30))));
    }

    @Test
    @DisplayName("Funcionamento: segunda a sábado, das 06:00 às 20:00 (última hora de início)")
    void horarioFuncionamento() {
        var validador = new ValidadorHorarioFuncionamento();
        assertDoesNotThrow(() -> validador.validar(em(LocalDateTime.of(2026, 10, 19, 6, 0))));
        assertDoesNotThrow(() -> validador.validar(em(LocalDateTime.of(2026, 10, 19, 20, 0))));
        assertThrows(ValidacaoException.class, () -> validador.validar(em(LocalDateTime.of(2026, 10, 19, 5, 0))));
        assertThrows(ValidacaoException.class, () -> validador.validar(em(LocalDateTime.of(2026, 10, 19, 21, 0))));
        assertThrows(ValidacaoException.class, () -> validador.validar(em(LocalDateTime.of(2026, 10, 18, 10, 0))));
    }

    @Test
    @DisplayName("Antecedência mínima de 30 minutos")
    void antecedencia() {
        var validador = new ValidadorHorarioAntecedencia();
        assertDoesNotThrow(() -> validador.validar(em(LocalDateTime.now().plusMinutes(45))));
        assertThrows(ValidacaoException.class, () -> validador.validar(em(LocalDateTime.now().plusMinutes(10))));
    }

    @Test
    @DisplayName("Limite diário: bloqueia segunda instrução do aluno no mesmo dia (06:00-20:00)")
    void limiteDiarioBloqueia() {
        when(instrucaoRepository.existsByAlunoIdAndDataHoraBetween(eq(1L), any(), any())).thenReturn(true);
        var validador = new ValidadorLimiteDiarioAluno(instrucaoRepository);

        assertThrows(ValidacaoException.class, () -> validador.validar(em(LocalDateTime.of(2026, 10, 19, 10, 0))));

        ArgumentCaptor<LocalDateTime> inicio = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> fim = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(instrucaoRepository).existsByAlunoIdAndDataHoraBetween(eq(1L), inicio.capture(), fim.capture());
        assertEquals(LocalDateTime.of(2026, 10, 19, 6, 0), inicio.getValue());
        assertEquals(LocalDateTime.of(2026, 10, 19, 20, 0), fim.getValue());
    }

    @Test
    @DisplayName("Limite diário: permite a primeira instrução do dia")
    void limiteDiarioPermite() {
        when(instrucaoRepository.existsByAlunoIdAndDataHoraBetween(eq(1L), any(), any())).thenReturn(false);
        var validador = new ValidadorLimiteDiarioAluno(instrucaoRepository);
        assertDoesNotThrow(() -> validador.validar(em(LocalDateTime.of(2026, 10, 19, 10, 0))));
    }

    @Test
    @DisplayName("Conflito de horário do instrutor")
    void conflitoHorarioInstrutor() {
        var data = LocalDateTime.of(2026, 10, 19, 10, 0);
        when(instrucaoRepository.existsByInstrutorIdAndDataHora(2L, data)).thenReturn(true);
        var validador = new ValidadorConflitoHorarioInstrutor(instrucaoRepository);
        assertThrows(ValidacaoException.class, () -> validador.validar(em(data)));
    }

    @Test
    @DisplayName("Aluno inativo não pode agendar")
    void alunoInativo() {
        when(alunoRepository.existsByIdAndAtivoFalse(1L)).thenReturn(true);
        var validador = new ValidadorAlunoAtivo(alunoRepository);
        assertThrows(ValidacaoException.class, () -> validador.validar(em(LocalDateTime.of(2026, 10, 19, 10, 0))));
    }

    @Test
    @DisplayName("Instrutor inativo não pode receber agendamento")
    void instrutorInativo() {
        when(instrutorRepository.existsByIdAndAtivoFalse(2L)).thenReturn(true);
        var validador = new ValidadorInstrutorAtivo(instrutorRepository);
        assertThrows(ValidacaoException.class, () -> validador.validar(em(LocalDateTime.of(2026, 10, 19, 10, 0))));
    }
}
