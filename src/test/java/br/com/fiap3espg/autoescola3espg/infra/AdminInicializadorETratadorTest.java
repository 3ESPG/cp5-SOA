package br.com.fiap3espg.autoescola3espg.infra;

import br.com.fiap3espg.autoescola3espg.domain.usuario.Role;
import br.com.fiap3espg.autoescola3espg.domain.usuario.Usuario;
import br.com.fiap3espg.autoescola3espg.domain.usuario.UsuarioRepository;
import br.com.fiap3espg.autoescola3espg.infra.exception.TratadorGlobalErros;
import br.com.fiap3espg.autoescola3espg.infra.security.AdminInicializador;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminInicializadorETratadorTest {
    @Mock
    UsuarioRepository repository;
    @Mock
    PasswordEncoder encoder;

    @Test
    @DisplayName("Sem ADMIN_PASSWORD nenhum usuário é criado")
    void semSenhaNaoCria() {
        new AdminInicializador(repository, encoder, "admin", "").run(null);
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Com usuários já cadastrados não cria o administrador")
    void jaExistemUsuarios() {
        when(repository.count()).thenReturn(3L);
        new AdminInicializador(repository, encoder, "admin", "segredo").run(null);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Tabela vazia + ADMIN_PASSWORD: cria ADMIN com senha codificada")
    void criaAdmin() {
        when(repository.count()).thenReturn(0L);
        when(encoder.encode("segredo")).thenReturn("HASH");

        new AdminInicializador(repository, encoder, "admin", "segredo").run(null);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        assertEquals("admin", captor.getValue().getLogin());
        assertEquals("HASH", captor.getValue().getSenha());
        assertEquals(Role.ADMIN, captor.getValue().getPerfil());
    }

    @Test
    @DisplayName("Tratador: violação de integridade vira 409")
    void integridade409() {
        var resposta = new TratadorGlobalErros().tratarIntegridade(new DataIntegrityViolationException("duplicado"));
        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
    }

    @Test
    @DisplayName("Tratador: falha de autenticação vira 401")
    void autenticacao401() {
        var resposta = new TratadorGlobalErros().tratarAutenticacao(new BadCredentialsException("x"));
        assertEquals(HttpStatus.UNAUTHORIZED, resposta.getStatusCode());
    }
}
