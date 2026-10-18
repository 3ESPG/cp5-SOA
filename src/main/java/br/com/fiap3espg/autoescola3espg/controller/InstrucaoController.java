package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosDetalhamentoAgendamento;
import br.com.fiap3espg.autoescola3espg.service.InstrucaoService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/instrucoes")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Instruções", description = "Agendamento, consulta e cancelamento de instruções")
@RequiredArgsConstructor
public class InstrucaoController {
    private final InstrucaoService service;

    @PostMapping
    @Operation(summary = "Agenda uma instrução",
            description = "Se o id do instrutor não for informado, um instrutor ativo e disponível da especialidade é escolhido aleatoriamente. Formato de data_hora: dd/MM/yyyy - HH:mm")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Instrução agendada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Aluno ou instrutor não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoAgendamento> agendarInstrucao(@RequestBody @Valid DadosAgendamentoInstrucao dados) {
        return ResponseEntity.ok(service.agendarInstrucao(dados));
    }

    @GetMapping
    @Operation(summary = "Lista as instruções agendadas (paginado)")
    @ApiResponse(responseCode = "200", description = "Página de instruções")
    public ResponseEntity<Page<DadosDetalhamentoAgendamento>> listarInstrucoes(
            @ParameterObject @PageableDefault(size = 10, sort = "dataHora") Pageable paginacao) {
        return ResponseEntity.ok(service.listarInstrucoes(paginacao));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma instrução")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Instrução encontrada"),
            @ApiResponse(responseCode = "404", description = "Instrução não encontrada")
    })
    public ResponseEntity<DadosDetalhamentoAgendamento> detalharInstrucao(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalharInstrucao(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela uma instrução")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Instrução cancelada"),
            @ApiResponse(responseCode = "404", description = "Instrução não encontrada")
    })
    public ResponseEntity<Void> cancelarInstrucao(@PathVariable Long id) {
        service.cancelarInstrucao(id);
        return ResponseEntity.noContent().build();
    }
}
