package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import br.com.fiap3espg.autoescola3espg.domain.endereco.Endereco;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstrutorServiceTest {
    @Mock
    InstrutorRepository repository;
    @InjectMocks
    InstrutorService service;

    private Instrutor instrutor() {
        return new Instrutor(1L, "Carlos", "carlos@email.com", "11999999999", "01234567890", Especialidade.CARROS,
                new Endereco("Rua A", "1", null, "Centro", "São Paulo", "SP", "01001-000"), true);
    }

    @Test
    @DisplayName("Cadastrar instrutor")
    void cadastrar() {
        when(repository.save(any(Instrutor.class))).thenAnswer(inv -> inv.getArgument(0));
        var dados = new DadosCadastroInstrutor("Carlos", "carlos@email.com", "11999999999", "01234567890",
                Especialidade.CARROS, new DadosEndereco("Rua A", "1", null, "Centro", "São Paulo", "SP", "01001-000"));

        var resultado = service.cadastrarInstrutor(dados);

        assertEquals("Carlos", resultado.nome());
        assertEquals(Especialidade.CARROS, resultado.especialidade());
        assertTrue(resultado.ativo());
    }

    @Test
    @DisplayName("Listar instrutores ativos")
    void listar() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAllByAtivoTrue(pageable)).thenReturn(new PageImpl<>(List.of(instrutor())));
        assertEquals(1, service.listarInstrutores(pageable).getTotalElements());
    }

    @Test
    @DisplayName("Detalhar instrutor inexistente lança InstrutorNotFoundException")
    void detalharInexistente() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(InstrutorNotFoundException.class, () -> service.detalharInstrutor(9L));
    }

    @Test
    @DisplayName("Atualizar instrutor existente")
    void atualizar() {
        when(repository.findById(1L)).thenReturn(Optional.of(instrutor()));
        when(repository.save(any(Instrutor.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = service.atualizarInstrutor(
                new DadosAtualizacaoInstrutor(1L, null, null, null, Especialidade.MOTOS, null));

        assertEquals(Especialidade.MOTOS, resultado.especialidade());
        assertEquals("Carlos", resultado.nome());
    }

    @Test
    @DisplayName("Excluir instrutor: exclusão lógica")
    void excluir() {
        Instrutor instrutor = instrutor();
        when(repository.findById(1L)).thenReturn(Optional.of(instrutor));

        service.excluirInstrutor(1L);

        assertFalse(instrutor.isAtivo());
        verify(repository).save(instrutor);
    }

    @Test
    @DisplayName("Excluir instrutor inexistente lança exceção")
    void excluirInexistente() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(InstrutorNotFoundException.class, () -> service.excluirInstrutor(9L));
    }
}
