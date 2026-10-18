package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import br.com.fiap3espg.autoescola3espg.domain.usuario.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DadosDetalhamentoUsuario cadastrarUsuario(DadosCadastroUsuario dados) {
        if (repository.existsByLogin(dados.login())) {
            throw new ValidacaoException("Login já cadastrado!");
        }
        Usuario usuario = new Usuario(dados.login(), passwordEncoder.encode(dados.senha()), dados.perfil());
        return new DadosDetalhamentoUsuario(repository.save(usuario));
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoUsuario> listarUsuarios(Pageable paginacao) {
        return repository.findAll(paginacao).map(DadosDetalhamentoUsuario::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoUsuario detalharUsuario(Long id) {
        return new DadosDetalhamentoUsuario(buscar(id));
    }

    @Transactional
    public DadosDetalhamentoUsuario atualizarUsuario(DadosAtualizacaoUsuario dados) {
        Usuario usuario = buscar(dados.id());
        if (dados.login() != null && !dados.login().isBlank()
                && repository.existsByLoginAndIdNot(dados.login(), dados.id())) {
            throw new ValidacaoException("Login já cadastrado!");
        }
        String senhaCodificada = (dados.senha() == null || dados.senha().isBlank())
                ? null
                : passwordEncoder.encode(dados.senha());
        usuario.atualizarInformacoes(dados.login(), senhaCodificada, dados.perfil());
        return new DadosDetalhamentoUsuario(repository.save(usuario));
    }

    @Transactional
    public void excluirUsuario(Long id) {
        repository.delete(buscar(id));
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("ID do usuário informado não existe!"));
    }
}
