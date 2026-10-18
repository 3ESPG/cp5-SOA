package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.integration.cep.CepService;
import br.com.fiap3espg.autoescola3espg.integration.cep.ConsultaCepResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/integracoes/cep")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Integração ViaCEP", description = "Consumo da API externa ViaCEP")
@RequiredArgsConstructor
public class CepController {
    private final CepService service;

    @GetMapping("/{cep}")
    @Operation(summary = "Consulta um CEP na API externa ViaCEP",
            description = "Aceita CEP com 8 dígitos, com ou sem hífen (ex.: 01001000 ou 01001-000).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
            @ApiResponse(responseCode = "400", description = "CEP com formato inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "CEP inexistente"),
            @ApiResponse(responseCode = "502", description = "ViaCEP indisponível ou com erro")
    })
    public ConsultaCepResponse consultar(@PathVariable String cep) {
        return service.consultar(cep);
    }
}
