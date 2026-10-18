package br.com.fiap3espg.autoescola3espg.domain.usuario;

/** Representação pública do usuário: nunca expõe a senha. */
public record DadosDetalhamentoUsuario(Long id, String login, Role perfil) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(usuario.getId(), usuario.getLogin(), usuario.getPerfil());
    }
}
