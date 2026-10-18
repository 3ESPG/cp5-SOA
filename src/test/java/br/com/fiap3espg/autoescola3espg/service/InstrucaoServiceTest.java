package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.aluno.Aluno;
import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoRepository;
import br.com.fiap3espg.autoescola3espg.domain.endereco.Endereco;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.*;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao.ValidadorAgendamento;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Especialidade;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Instrutor;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.InstrutorNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.InstrutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Testa agendamento e cancelamento de instruções (regras de negócio na camada de serviço). */
@ExtendWith(MockitoExtension.class)
class InstrucaoServiceTest {
    @Mock
    InstrucaoRepository repository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    InstrutorRepository instrutorRepository;
    @Mock
    ValidadorAgendamento validador;

    InstrucaoService service;

    private final LocalDateTime dataHora = LocalDateTime.of(2026, 10, 19, 10, 0);
    private final Endereco endereco = new Endereco("Rua A", "1", null, "Centro", "São Paulo", "SP", "01001-000");
    private final Aluno aluno = new Aluno(1L, "Maria", "maria@email.com", "11999999999", "12345678901", endereco, true);
    private final Instrutor instrutor = new Instrutor(2L, "Carlos", "carlos@email.com", "11999999999", "01234567890",
            Especialidade.CARROS, endereco, true);

    @BeforeEach
    void setUp() {
        service = new InstrucaoService(repository, alunoRepository, instrutorRepository, List.of(validador));
    }

    private void simularSalvamento() {
        when(repository.save(any(Instrucao.class))).thenAnswer(inv -> {
            Instrucao i = inv.getArgument(0);
            return new Instrucao(10L, i.getAluno(), i.getInstrutor(), i.getDataHora());
        });
    }

    @Test
    @DisplayName("Agendar com instrutor informado: valida, salva e detalha")
    void agendarComInstrutor() {
        var dados = new DadosAgendamentoInstrucao(1L, 2L, null, dataHora);
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(instrutorRepository.existsById(2L)).thenReturn(true);
        when(alunoRepository.getReferenceById(1L)).thenReturn(aluno);
        when(instrutorRepository.getReferenceById(2L)).thenReturn(instrutor);
        simularSalvamento();

        DadosDetalhamentoAgendamento resultado = service.agendarInstrucao(dados);

        assertEquals(10L, resultado.id());
        assertEquals("Maria", resultado.nomeAluno());
        assertEquals("Carlos", resultado.nomeInstrutor());
        assertEquals(dataHora, resultado.dataHora());
        verify(validador).validar(dados);
    }

    @Test
    @DisplayName("Agendar sem instrutor: escolhe instrutor disponível da especialidade")
    void agendarEscolhendoInstrutor() {
        var dados = new DadosAgendamentoInstrucao(1L, null, Especialidade.CARROS, dataHora);
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(alunoRepository.getReferenceById(1L)).thenReturn(aluno);
        when(instrutorRepository.escolherInstrutorAleatorioDisponivel(Especialidade.CARROS, dataHora)).thenReturn(instrutor);
        simularSalvamento();

        assertEquals("Carlos", service.agendarInstrucao(dados).nomeInstrutor());
    }

    @Test
    @DisplayName("Agendar sem instrutor disponível: lança ValidacaoException e não salva")
    void agendarSemInstrutorDisponivel() {
        var dados = new DadosAgendamentoInstrucao(1L, null, Especialidade.CARROS, dataHora);
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(alunoRepository.getReferenceById(1L)).thenReturn(aluno);
        when(instrutorRepository.escolherInstrutorAleatorioDisponivel(Especialidade.CARROS, dataHora)).thenReturn(null);

        assertThrows(ValidacaoException.class, () -> service.agendarInstrucao(dados));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Agendar sem instrutor e sem especialidade: lança ValidacaoException")
    void agendarSemInstrutorESemEspecialidade() {
        var dados = new DadosAgendamentoInstrucao(1L, null, null, dataHora);
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(alunoRepository.getReferenceById(1L)).thenReturn(aluno);

        assertThrows(ValidacaoException.class, () -> service.agendarInstrucao(dados));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Agendar com aluno inexistente: AlunoNotFoundException, sem executar validadores")
    void agendarAlunoInexistente() {
        when(alunoRepository.existsById(1L)).thenReturn(false);
        assertThrows(AlunoNotFoundException.class,
                () -> service.agendarInstrucao(new DadosAgendamentoInstrucao(1L, 2L, null, dataHora)));
        verifyNoInteractions(validador);
    }

    @Test
    @DisplayName("Agendar com instrutor inexistente: InstrutorNotFoundException")
    void agendarInstrutorInexistente() {
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(instrutorRepository.existsById(2L)).thenReturn(false);
        assertThrows(InstrutorNotFoundException.class,
                () -> service.agendarInstrucao(new DadosAgendamentoInstrucao(1L, 2L, null, dataHora)));
        verifyNoInteractions(validador);
    }

    @Test
    @DisplayName("Agendar: regra de negócio violada impede o salvamento")
    void agendarRegraViolada() {
        var dados = new DadosAgendamentoInstrucao(1L, 2L, null, dataHora);
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(instrutorRepository.existsById(2L)).thenReturn(true);
        doThrow(new ValidacaoException("Instrutor ocupado na data / hora escolhida!")).when(validador).validar(dados);

        var ex = assertThrows(ValidacaoException.class, () -> service.agendarInstrucao(dados));

        assertEquals("Instrutor ocupado na data / hora escolhida!", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Cancelar: remove a instrução existente")
    void cancelar() {
        Instrucao instrucao = new Instrucao(10L, aluno, instrutor, dataHora);
        when(repository.findById(10L)).thenReturn(Optional.of(instrucao));

        service.cancelarInstrucao(10L);

        verify(repository).delete(instrucao);
    }

    @Test
    @DisplayName("Cancelar: instrução inexistente lança InstrucaoNotFoundException")
    void cancelarInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(InstrucaoNotFoundException.class, () -> service.cancelarInstrucao(99L));
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Detalhar instrução existente e inexistente")
    void detalhar() {
        when(repository.findById(10L)).thenReturn(Optional.of(new Instrucao(10L, aluno, instrutor, dataHora)));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertEquals("Maria", service.detalharInstrucao(10L).nomeAluno());
        assertThrows(InstrucaoNotFoundException.class, () -> service.detalharInstrucao(99L));
    }
}
