package br.com.fiap3espg.autoescola3espg.infra.security;

import br.com.fiap3espg.autoescola3espg.domain.usuario.Role;
import br.com.fiap3espg.autoescola3espg.domain.usuario.Usuario;
import br.com.fiap3espg.autoescola3espg.domain.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro usuário ADMIN quando a tabela de usuários está vazia e a variável
 * de ambiente ADMIN_PASSWORD foi informada. Nenhuma credencial fica no código-fonte.
 */
@Component
public class AdminInicializador implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminInicializador.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String login;
    private final String senha;

    public AdminInicializador(UsuarioRepository repository,
                              PasswordEncoder passwordEncoder,
                              @Value("${app.admin.login:admin}") String login,
                              @Value("${app.admin.password:}") String senha) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.login = login;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (senha == null || senha.isBlank()) {
            log.info("ADMIN_PASSWORD não definida: nenhum usuário administrador inicial será criado.");
            return;
        }
        if (repository.count() > 0) {
            return;
        }
        repository.save(new Usuario(login, passwordEncoder.encode(senha), Role.ADMIN));
        log.info("Usuário administrador inicial '{}' criado.", login);
    }
}
