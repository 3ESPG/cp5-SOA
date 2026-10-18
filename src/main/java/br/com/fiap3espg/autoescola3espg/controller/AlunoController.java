package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.aluno.*;
import br.com.fiap3espg.autoescola3espg.service.AlunoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/alunos")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Alunos", description = "CRUD de alunos da autoescola")
@RequiredArgsConstructor
public class AlunoController {
    private final AlunoService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra um aluno")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aluno cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão"),
            @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado")
    })
    public ResponseEntity<DadosDetalhamentoAluno> cadastrarAluno(
            @RequestBody @Valid DadosCadastroAluno dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoAluno dto = service.cadastrarAluno(dados);
        URI uri = uriBuilder.path("/alunos/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Lista os alunos ativos (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de alunos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    public ResponseEntity<Page<DadosListagemAluno>> listarAlunos(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(service.listarAlunos(paginacao));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Detalha um aluno")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno encontrado"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoAluno> detalharAluno(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalharAluno(id));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza dados de um aluno")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoAluno> atualizarAluno(
            @RequestBody @Valid DadosAtualizacaoAluno dados) {
        return ResponseEntity.ok(service.atualizarAluno(dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inativa um aluno (exclusão lógica)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Aluno inativado"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado")
    })
    public ResponseEntity<Void> excluirAluno(@PathVariable Long id) {
        service.excluirAluno(id);
        return ResponseEntity.noContent().build();
    }
}
