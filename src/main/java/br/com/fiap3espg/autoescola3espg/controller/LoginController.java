package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.usuario.DadosLogin;
import br.com.fiap3espg.autoescola3espg.domain.usuario.Usuario;
import br.com.fiap3espg.autoescola3espg.infra.security.DadosTokenJWT;
import br.com.fiap3espg.autoescola3espg.infra.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@Tag(name = "Autenticação", description = "Geração do token JWT")
@RequiredArgsConstructor
public class LoginController {
    private final AuthenticationManager manager;
    private final TokenService tokenService;

    @PostMapping
    @Operation(summary = "Efetua login e retorna o token JWT (válido por 30 minutos)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login efetuado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Login ou senha inválidos")
    })
    public ResponseEntity<DadosTokenJWT> efetuarLogin(@RequestBody @Valid DadosLogin dados) {
        var token = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());
        Authentication authentication = manager.authenticate(token);
        String tokenJWT = tokenService.generateToken((Usuario) authentication.getPrincipal());
        return ResponseEntity.ok(new DadosTokenJWT(tokenJWT));
    }
}
