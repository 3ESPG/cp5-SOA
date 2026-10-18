package br.com.fiap3espg.autoescola3espg.domain;

import br.com.fiap3espg.autoescola3espg.domain.aluno.*;
import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import br.com.fiap3espg.autoescola3espg.domain.endereco.Endereco;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.Instrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.*;
import br.com.fiap3espg.autoescola3espg.domain.usuario.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntidadesTest {
    private Endereco endereco() {
        return new Endereco("Rua Teste", "10", null, "Centro", "São Paulo", "SP", "01001-000");
    }

    private DadosEndereco dadosEndereco() {
        return new DadosEndereco("Rua Nova", "20", "Apto 1", "Bairro Novo", "Campinas", "SP", "13000-000");
    }

    // ---------------- Aluno ----------------
    @Test
    @DisplayName("Aluno: criado a partir do DTO de cadastro fica ativo")
    void alunoCriadoPeloDtoFicaAtivo() {
        Aluno aluno = new Aluno(new DadosCadastroAluno("Maria", "maria@email.com", "11999999999", "12345678901", dadosEndereco()));
        assertEquals("Maria", aluno.getNome());
        assertEquals("Campinas", aluno.getEndereco().getCidade());
        assertTrue(aluno.isAtivo());
    }

    @Test
    @DisplayName("Aluno: atualização parcial altera só os campos informados")
    void alunoAtualizacaoParcial() {
        Aluno aluno = new Aluno(1L, "Aluno", "aluno@email.com", "11999999999", "12345678901", endereco(), true);
        aluno.atualizarInformacoes(new DadosAtualizacaoAluno(1L, "Novo Nome", null, " ", null));
        assertEquals("Novo Nome", aluno.getNome());
        assertEquals("aluno@email.com", aluno.getEmail());
        assertEquals("11999999999", aluno.getTelefone());
    }

    @Test
    @DisplayName("Aluno: exclusão é lógica (ativo = false)")
    void alunoExclusaoLogica() {
        Aluno aluno = new Aluno(1L, "Aluno", "aluno@email.com", "11999999999", "12345678901", endereco(), true);
        aluno.excluir();
        assertFalse(aluno.isAtivo());
    }

    // ---------------- Instrutor ----------------
    @Test
    void deveCriarInstrutorEExcluirLogicamente() {
        Instrutor instrutor = new Instrutor(2L, "Instrutor", "instrutor@email.com", "11999999999", "12345678901", Especialidade.MOTOS, endereco(), true);
        assertEquals(Especialidade.MOTOS, instrutor.getEspecialidade());
        instrutor.excluir();
        assertFalse(instrutor.isAtivo());
    }

    @Test
    @DisplayName("Instrutor: atualização altera especialidade e endereço, preservando o restante")
    void instrutorAtualizacao() {
        Instrutor instrutor = new Instrutor(2L, "Instrutor", "instrutor@email.com", "11999999999", "12345678901", Especialidade.MOTOS, endereco(), true);
        instrutor.atualizarInformacoes(new DadosAtualizacaoInstrutor(2L, null, null, "11888888888", Especialidade.CARROS, dadosEndereco()));
        assertEquals(Especialidade.CARROS, instrutor.getEspecialidade());
        assertEquals("11888888888", instrutor.getTelefone());
        assertEquals("Instrutor", instrutor.getNome());
        assertEquals("Campinas", instrutor.getEndereco().getCidade());
    }

    // ---------------- Instrução ----------------
    @Test
    void deveAssociarInstrucaoAoAlunoEInstrutor() {
        Aluno aluno = new Aluno(1L, "Aluno", "aluno@email.com", "11999999999", "12345678901", endereco(), true);
        Instrutor instrutor = new Instrutor(2L, "Instrutor", "instrutor@email.com", "11999999999", "12345678901", Especialidade.MOTOS, endereco(), true);
        LocalDateTime horario = LocalDateTime.of(2026, 10, 19, 10, 0);
        Instrucao instrucao = new Instrucao(3L, aluno, instrutor, horario);
        assertEquals(aluno, instrucao.getAluno());
        assertEquals(instrutor, instrucao.getInstrutor());
        assertEquals(horario, instrucao.getDataHora());
    }

    // ---------------- Usuário ----------------
    @Test
    void deveCriarUsuarioComPerfil() {
        Usuario usuario = new Usuario(4L, "admin", "hash-exemplo", Role.ADMIN);
        assertEquals("admin", usuario.getUsername());
        assertEquals("hash-exemplo", usuario.getPassword());
        assertTrue(usuario.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Usuário: atualização ignora campos nulos/em branco")
    void usuarioAtualizacaoParcial() {
        Usuario usuario = new Usuario("joao", "hash1", Role.USER);
        usuario.atualizarInformacoes(null, " ", Role.ADMIN);
        assertEquals("joao", usuario.getLogin());
        assertEquals("hash1", usuario.getSenha());
        assertEquals(Role.ADMIN, usuario.getPerfil());
        usuario.atualizarInformacoes("joao2", "hash2", null);
        assertEquals("joao2", usuario.getLogin());
        assertEquals("hash2", usuario.getSenha());
        assertEquals(Role.ADMIN, usuario.getPerfil());
    }

    @Test
    @DisplayName("Usuário: DTO de detalhamento não expõe a senha")
    void detalhamentoDeUsuarioNaoExpoeSenha() {
        DadosDetalhamentoUsuario dto = new DadosDetalhamentoUsuario(new Usuario(5L, "maria", "hash", Role.USER));
        assertEquals("maria", dto.login());
        assertFalse(dto.toString().contains("hash"));
    }
}
