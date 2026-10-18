package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.InstrucaoRepository;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ValidadorLimiteDiarioAluno implements ValidadorAgendamento {
    private final InstrucaoRepository repository;

    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        LocalDateTime inicioExpediente = dados.dataHora().toLocalDate().atTime(6, 0);
        LocalDateTime fimExpediente = dados.dataHora().toLocalDate().atTime(21 - 1, 0);

        boolean reincidencia = repository.existsByAlunoIdAndDataHoraBetween(dados.idAluno(), inicioExpediente, fimExpediente);

        if (reincidencia) {
            throw new ValidacaoException("Permitido o agendamento diário de apenas uma instrução por aluno!");
        }
    }
}