package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.aluno.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlunoService {
    private final AlunoRepository repository;

    @Transactional
    public DadosDetalhamentoAluno cadastrarAluno(DadosCadastroAluno dados) {
        Aluno salvo = repository.save(new Aluno(dados));
        return new DadosDetalhamentoAluno(salvo);
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemAluno> listarAlunos(Pageable paginacao) {
        return repository.findAllByAtivoTrue(paginacao).map(DadosListagemAluno::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoAluno detalharAluno(Long id) {
        return new DadosDetalhamentoAluno(buscar(id));
    }

    @Transactional
    public DadosDetalhamentoAluno atualizarAluno(DadosAtualizacaoAluno dados) {
        Aluno aluno = buscar(dados.id());
        aluno.atualizarInformacoes(dados);
        return new DadosDetalhamentoAluno(repository.save(aluno));
    }

    @Transactional
    public void excluirAluno(Long id) {
        Aluno aluno = buscar(id);
        aluno.excluir();
        repository.save(aluno);
    }

    private Aluno buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AlunoNotFoundException("ID do aluno informado não existe!"));
    }
}
