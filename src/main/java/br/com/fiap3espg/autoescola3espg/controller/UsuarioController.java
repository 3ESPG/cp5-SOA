package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.usuario.*;
import br.com.fiap3espg.autoescola3espg.service.UsuarioService;
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
@RequestMapping("/usuarios")
@SecurityRequirement(name = "bearer-key")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuários", description = "CRUD de usuários (somente perfil ADMIN)")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService service;

    @PostMapping
    @Operation(summary = "Cadastra um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou login já cadastrado"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    })
    public ResponseEntity<DadosDetalhamentoUsuario> cadastrarUsuario(
            @RequestBody @Valid DadosCadastroUsuario dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoUsuario dto = service.cadastrarUsuario(dados);
        URI uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    @Operation(summary = "Lista os usuários (paginado)")
    public ResponseEntity<Page<DadosDetalhamentoUsuario>> listarUsuarios(
            @ParameterObject @PageableDefault(size = 10, sort = "login") Pageable paginacao) {
        return ResponseEntity.ok(service.listarUsuarios(paginacao));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoUsuario> detalharUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalharUsuario(id));
    }

    @PutMapping
    @Operation(summary = "Atualiza login, senha e/ou perfil de um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou login já cadastrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoUsuario> atualizarUsuario(
            @RequestBody @Valid DadosAtualizacaoUsuario dados) {
        return ResponseEntity.ok(service.atualizarUsuario(dados));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário excluído"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> excluirUsuario(@PathVariable Long id) {
        service.excluirUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
