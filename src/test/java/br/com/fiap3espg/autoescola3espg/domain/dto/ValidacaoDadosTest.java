package br.com.fiap3espg.autoescola3espg.domain.dto;

import br.com.fiap3espg.autoescola3espg.domain.aluno.DadosCadastroAluno;
import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.DadosCadastroInstrutor;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Especialidade;
import br.com.fiap3espg.autoescola3espg.domain.usuario.DadosCadastroUsuario;
import br.com.fiap3espg.autoescola3espg.domain.usuario.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Testes de validação (Bean Validation) dos DTOs de entrada de cada entidade. */
class ValidacaoDadosTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private DadosEndereco enderecoValido() {
        return new DadosEndereco("Rua A", "1", null, "Centro", "São Paulo", "SP", "01001-000");
    }

    private <T> boolean temErroEm(Set<ConstraintViolation<T>> violacoes, String campo) {
        return violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().startsWith(campo));
    }

    @Test
    @DisplayName("Aluno válido não gera violações")
    void alunoValido() {
        var dados = new DadosCadastroAluno("Maria", "maria@email.com", "11999999999", "12345678901", enderecoValido());
        assertTrue(validator.validate(dados).isEmpty());
    }

    @Test
    @DisplayName("Aluno: CPF, e-mail, nome e endereço inválidos são rejeitados")
    void alunoInvalido() {
        var dados = new DadosCadastroAluno("", "nao-e-email", "11999999999", "123", null);
        var violacoes = validator.validate(dados);
        assertTrue(temErroEm(violacoes, "nome"));
        assertTrue(temErroEm(violacoes, "email"));
        assertTrue(temErroEm(violacoes, "cpf"));
        assertTrue(temErroEm(violacoes, "endereco"));
    }

    @Test
    @DisplayName("Instrutor: CNH fora do padrão e endereço ausente são rejeitados")
    void instrutorInvalido() {
        var dados = new DadosCadastroInstrutor("Joao", "joao@email.com", "11999999999", "abc", Especialidade.CARROS, null);
        var violacoes = validator.validate(dados);
        assertTrue(temErroEm(violacoes, "cnh"));
        assertTrue(temErroEm(violacoes, "endereco"));
    }

    @Test
    @DisplayName("Instrutor válido não gera violações")
    void instrutorValido() {
        var dados = new DadosCadastroInstrutor("Joao", "joao@email.com", "11999999999", "01234567890", Especialidade.CARROS, enderecoValido());
        assertTrue(validator.validate(dados).isEmpty());
    }

    @Test
    @DisplayName("Usuário: senha curta, login em branco e perfil ausente são rejeitados")
    void usuarioInvalido() {
        var violacoes = validator.validate(new DadosCadastroUsuario(" ", "123", null));
        assertTrue(temErroEm(violacoes, "login"));
        assertTrue(temErroEm(violacoes, "senha"));
        assertTrue(temErroEm(violacoes, "perfil"));
    }

    @Test
    @DisplayName("Usuário válido não gera violações")
    void usuarioValido() {
        assertTrue(validator.validate(new DadosCadastroUsuario("maria", "senha123", Role.USER)).isEmpty());
    }

    @Test
    @DisplayName("Instrução: data no passado e aluno ausente são rejeitados")
    void instrucaoInvalida() {
        var dados = new DadosAgendamentoInstrucao(null, 1L, Especialidade.MOTOS, LocalDateTime.now().minusDays(1));
        var violacoes = validator.validate(dados);
        assertTrue(temErroEm(violacoes, "idAluno"));
        assertTrue(temErroEm(violacoes, "dataHora"));
    }

    @Test
    @DisplayName("Endereço: UF e CEP fora do padrão são rejeitados")
    void enderecoInvalido() {
        var endereco = new DadosEndereco("Rua A", "1", null, "Centro", "São Paulo", "sp", "01001000");
        var violacoes = validator.validate(endereco);
        assertTrue(temErroEm(violacoes, "uf"));
        assertTrue(temErroEm(violacoes, "cep"));
    }
}
