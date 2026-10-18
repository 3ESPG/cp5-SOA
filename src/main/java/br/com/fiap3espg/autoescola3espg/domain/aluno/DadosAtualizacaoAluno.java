package br.com.fiap3espg.autoescola3espg.domain.aluno;

import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoAluno(
        @NotNull
        Long id,
        String nome,
        String email,
        String telefone,
        DadosEndereco endereco) {
}
