package br.com.fiap3espg.autoescola3espg.domain.instrucao;

import br.com.fiap3espg.autoescola3espg.domain.instrutor.Especialidade;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record DadosDetalhamentoAgendamento(
        Long id,
        String nomeAluno,
        String nomeInstrutor,
        Especialidade especialidade,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm")
        LocalDateTime dataHora) {
    public DadosDetalhamentoAgendamento(Instrucao instrucao) {
        this(
                instrucao.getId(),
                instrucao.getAluno().getNome(),
                instrucao.getInstrutor().getNome(),
                instrucao.getInstrutor().getEspecialidade(),
                instrucao.getDataHora()
        );
    }
}