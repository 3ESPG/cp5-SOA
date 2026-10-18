package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.aluno.*;
import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import br.com.fiap3espg.autoescola3espg.domain.endereco.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {
    @Mock
    AlunoRepository repository;
    @InjectMocks
    AlunoService service;

    private Aluno aluno() {
        return new Aluno(1L, "Maria", "maria@email.com", "11999999999", "12345678901",
                new Endereco("Rua A", "1", null, "Centro", "São Paulo", "SP", "01001-000"), true);
    }

    @Test
    @DisplayName("Cadastrar: persiste o aluno ativo e devolve o detalhamento")
    void cadastrar() {
        when(repository.save(any(Aluno.class))).thenAnswer(inv -> inv.getArgument(0));
        var dados = new DadosCadastroAluno("Maria", "maria@email.com", "11999999999", "12345678901",
                new DadosEndereco("Rua A", "1", null, "Centro", "São Paulo", "SP", "01001-000"));

        DadosDetalhamentoAluno resultado = service.cadastrarAluno(dados);

        assertEquals("Maria", resultado.nome());
        assertEquals("12345678901", resultado.cpf());
        assertTrue(resultado.ativo());
        verify(repository).save(any(Aluno.class));
    }

    @Test
    @DisplayName("Listar: devolve somente alunos ativos, paginados")
    void listar() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAllByAtivoTrue(pageable)).thenReturn(new PageImpl<>(List.of(aluno())));

        Page<DadosListagemAluno> pagina = service.listarAlunos(pageable);

        assertEquals(1, pagina.getTotalElements());
        assertEquals("Maria", pagina.getContent().get(0).nome());
    }

    @Test
    @DisplayName("Detalhar: aluno existente")
    void detalhar() {
        when(repository.findById(1L)).thenReturn(Optional.of(aluno()));
        assertEquals("maria@email.com", service.detalharAluno(1L).email());
    }

    @Test
    @DisplayName("Detalhar: aluno inexistente lança AlunoNotFoundException")
    void detalharInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AlunoNotFoundException.class, () -> service.detalharAluno(99L));
    }

    @Test
    @DisplayName("Atualizar: altera apenas os campos informados")
    void atualizar() {
        when(repository.findById(1L)).thenReturn(Optional.of(aluno()));
        when(repository.save(any(Aluno.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = service.atualizarAluno(new DadosAtualizacaoAluno(1L, "Maria Silva", null, null, null));

        assertEquals("Maria Silva", resultado.nome());
        assertEquals("maria@email.com", resultado.email());
    }

    @Test
    @DisplayName("Atualizar: aluno inexistente lança exceção e não salva")
    void atualizarInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AlunoNotFoundException.class,
                () -> service.atualizarAluno(new DadosAtualizacaoAluno(99L, "X", null, null, null)));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Excluir: inativa o aluno (exclusão lógica)")
    void excluir() {
        Aluno aluno = aluno();
        when(repository.findById(1L)).thenReturn(Optional.of(aluno));

        service.excluirAluno(1L);

        assertFalse(aluno.isAtivo());
        verify(repository).save(aluno);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Excluir: aluno inexistente lança exceção")
    void excluirInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AlunoNotFoundException.class, () -> service.excluirAluno(99L));
    }
}
