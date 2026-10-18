package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import br.com.fiap3espg.autoescola3espg.domain.usuario.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock
    UsuarioRepository repository;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    UsuarioService service;

    @Test
    @DisplayName("Cadastrar: a senha é codificada antes de persistir")
    void cadastrarCodificaSenha() {
        when(repository.existsByLogin("maria")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("HASH");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        DadosDetalhamentoUsuario resultado = service.cadastrarUsuario(new DadosCadastroUsuario("maria", "senha123", Role.USER));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        assertEquals("HASH", captor.getValue().getSenha());
        assertEquals("maria", resultado.login());
        assertEquals(Role.USER, resultado.perfil());
    }

    @Test
    @DisplayName("Cadastrar: login duplicado lança ValidacaoException e não salva")
    void cadastrarLoginDuplicado() {
        when(repository.existsByLogin("maria")).thenReturn(true);
        assertThrows(ValidacaoException.class,
                () -> service.cadastrarUsuario(new DadosCadastroUsuario("maria", "senha123", Role.USER)));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Detalhar: usuário inexistente lança UsuarioNotFoundException")
    void detalharInexistente() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(UsuarioNotFoundException.class, () -> service.detalharUsuario(9L));
    }

    @Test
    @DisplayName("Atualizar sem senha: mantém a senha atual e não chama o encoder")
    void atualizarSemSenha() {
        Usuario usuario = new Usuario(1L, "maria", "HASH-ANTIGO", Role.USER);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = service.atualizarUsuario(new DadosAtualizacaoUsuario(1L, null, null, Role.ADMIN));

        assertEquals(Role.ADMIN, resultado.perfil());
        assertEquals("HASH-ANTIGO", usuario.getSenha());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Atualizar com nova senha: codifica a senha")
    void atualizarComSenha() {
        Usuario usuario = new Usuario(1L, "maria", "HASH-ANTIGO", Role.USER);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("nova-senha")).thenReturn("HASH-NOVO");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        service.atualizarUsuario(new DadosAtualizacaoUsuario(1L, null, "nova-senha", null));

        assertEquals("HASH-NOVO", usuario.getSenha());
    }

    @Test
    @DisplayName("Atualizar: novo login já usado por outro usuário é rejeitado")
    void atualizarLoginDuplicado() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Usuario(1L, "maria", "H", Role.USER)));
        when(repository.existsByLoginAndIdNot("joao", 1L)).thenReturn(true);
        assertThrows(ValidacaoException.class,
                () -> service.atualizarUsuario(new DadosAtualizacaoUsuario(1L, "joao", null, null)));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Excluir: remove o usuário existente")
    void excluir() {
        Usuario usuario = new Usuario(1L, "maria", "H", Role.USER);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        service.excluirUsuario(1L);
        verify(repository).delete(usuario);
    }

    @Test
    @DisplayName("Excluir: usuário inexistente lança exceção")
    void excluirInexistente() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(UsuarioNotFoundException.class, () -> service.excluirUsuario(9L));
    }
}
