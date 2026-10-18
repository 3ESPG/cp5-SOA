package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.aluno.Aluno;
import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoRepository;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.*;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao.ValidadorAgendamento;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Instrutor;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.InstrutorNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.InstrutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstrucaoService {
    private final InstrucaoRepository repository;
    private final AlunoRepository alunoRepository;
    private final InstrutorRepository instrutorRepository;
    private final List<ValidadorAgendamento> validadoresAgendamento;

    @Transactional
    public DadosDetalhamentoAgendamento agendarInstrucao(DadosAgendamentoInstrucao dados) {
        if (!alunoRepository.existsById(dados.idAluno())) {
            throw new AlunoNotFoundException("ID do aluno informado não existe!");
        }
        if (dados.idInstrutor() != null && !instrutorRepository.existsById(dados.idInstrutor())) {
            throw new InstrutorNotFoundException("ID do instrutor informado não existe!");
        }
        //Validações (regras de negócio dos checkpoints anteriores)
        validadoresAgendamento.forEach(validador -> validador.validar(dados));

        Aluno aluno = alunoRepository.getReferenceById(dados.idAluno());
        Instrutor instrutor = escolherInstrutor(dados);
        if (instrutor == null) {
            throw new ValidacaoException("Nenhum instrutor disponível para a data/hora informada!");
        }

        Instrucao instrucao = new Instrucao(null, aluno, instrutor, dados.dataHora());
        Instrucao salvo = repository.save(instrucao);
        return new DadosDetalhamentoAgendamento(salvo);
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoAgendamento> listarInstrucoes(Pageable paginacao) {
        return repository.findAll(paginacao).map(DadosDetalhamentoAgendamento::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoAgendamento detalharInstrucao(Long id) {
        return new DadosDetalhamentoAgendamento(buscar(id));
    }

    /**
     * Cancela (remove) a instrução, liberando o horário do instrutor e do aluno.
     * Regras adicionais de cancelamento (ex.: antecedência mínima, motivo) dependem do
     * documento de regras de negócio dos checkpoints anteriores, que não foi fornecido.
     */
    @Transactional
    public void cancelarInstrucao(Long id) {
        repository.delete(buscar(id));
    }

    private Instrucao buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new InstrucaoNotFoundException("ID da instrução informado não existe!"));
    }

    private Instrutor escolherInstrutor(DadosAgendamentoInstrucao dados) {
        if (dados.idInstrutor() != null) {
            return instrutorRepository.getReferenceById(dados.idInstrutor());
        }
        if (dados.especialidade() == null) {
            throw new ValidacaoException("Especialidade é obrigatória se o instrutor não for informado!");
        }
        return instrutorRepository.escolherInstrutorAleatorioDisponivel(
                dados.especialidade(),
                dados.dataHora()
        );
    }
}
