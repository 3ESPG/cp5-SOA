package br.com.fiap3espg.autoescola3espg.domain.aluno;

import br.com.fiap3espg.autoescola3espg.domain.endereco.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AlunoRepositoryTest {
    @Autowired
    AlunoRepository repository;

    @Autowired
    TestEntityManager testEntity;

    private Aluno persistir(String email, String cpf, boolean ativo) {
        Endereco endereco = new Endereco("Rua A", "1", null, "Centro", "São Paulo", "SP", "01001-000");
        Aluno aluno = new Aluno(null, "Aluno", email, "11999999999", cpf, endereco, ativo);
        testEntity.persistAndFlush(aluno);
        return aluno;
    }

    @Test
    @DisplayName("Salva e recupera aluno com endereço embutido")
    void salvarERecuperar() {
        Aluno salvo = persistir("a@email.com", "11111111111", true);
        Aluno encontrado = repository.findById(salvo.getId()).orElseThrow();
        assertThat(encontrado.getEndereco().getCidade()).isEqualTo("São Paulo");
        assertThat(encontrado.getCpf()).isEqualTo("11111111111");
    }

    @Test
    @DisplayName("Listagem retorna somente alunos ativos")
    void listarSomenteAtivos() {
        Aluno ativo = persistir("ativo@email.com", "22222222222", true);
        persistir("inativo@email.com", "33333333333", false);
        assertThat(repository.findAllByAtivoTrue(PageRequest.of(0, 10)).getContent()).containsExactly(ativo);
    }

    @Test
    @DisplayName("existsByIdAndAtivoFalse identifica aluno inativo")
    void existsAtivoFalse() {
        Aluno inativo = persistir("inativo@email.com", "44444444444", false);
        Aluno ativo = persistir("ativo@email.com", "55555555555", true);
        assertThat(repository.existsByIdAndAtivoFalse(inativo.getId())).isTrue();
        assertThat(repository.existsByIdAndAtivoFalse(ativo.getId())).isFalse();
    }
}
