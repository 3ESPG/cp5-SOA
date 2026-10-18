package br.com.fiap3espg.autoescola3espg.domain.usuario;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoUsuario(
        @NotNull
        Long id,

        @Size(max = 100)
        String login,

        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres")
        String senha,

        Role perfil) {
}
