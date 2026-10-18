package br.com.fiap3espg.autoescola3espg.integration.cep;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import org.springframework.http.HttpMethod;

/** Simula as respostas do ViaCEP com MockRestServiceServer: nenhum acesso real à rede. */
class CepServiceTest {
    private MockRestServiceServer server;
    private CepService service;

    private static final String RESPOSTA_OK = """
            {"cep":"01001-000","logradouro":"Praça da Sé","complemento":"lado ímpar","unidade":"","bairro":"Sé",
             "localidade":"São Paulo","uf":"SP","estado":"São Paulo","regiao":"Sudeste","ibge":"3550308",
             "gia":"1004","ddd":"11","siafi":"7107"}
            """;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://viacep.com.br/ws");
        server = MockRestServiceServer.bindTo(builder).build();
        service = new CepService(builder.build());
    }

    @Test
    @DisplayName("Consulta CEP válido e mapeia o JSON do ViaCEP (campos extras são ignorados)")
    void consultaComSucesso() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(RESPOSTA_OK, MediaType.APPLICATION_JSON));

        ConsultaCepResponse resposta = service.consultar("01001000");

        assertEquals("01001-000", resposta.cep());
        assertEquals("Praça da Sé", resposta.logradouro());
        assertEquals("Sé", resposta.bairro());
        assertEquals("São Paulo", resposta.localidade());
        assertEquals("SP", resposta.uf());
        assertFalse(resposta.indicaErro());
        server.verify();
    }

    @Test
    @DisplayName("Aceita CEP com hífen e normaliza antes de chamar a API")
    void aceitaCepComHifen() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess(RESPOSTA_OK, MediaType.APPLICATION_JSON));
        assertEquals("SP", service.consultar("01001-000").uf());
        server.verify();
    }

    @Test
    @DisplayName("CEP inexistente (erro:true) lança CepNaoEncontradoException")
    void cepInexistenteBooleano() {
        server.expect(requestTo("https://viacep.com.br/ws/99999999/json/"))
                .andRespond(withSuccess("{\"erro\": true}", MediaType.APPLICATION_JSON));
        assertThrows(CepNaoEncontradoException.class, () -> service.consultar("99999999"));
        server.verify();
    }

    @Test
    @DisplayName("CEP inexistente (erro:\"true\" como texto) também é tratado")
    void cepInexistenteTexto() {
        server.expect(requestTo("https://viacep.com.br/ws/99999999/json/"))
                .andRespond(withSuccess("{\"erro\": \"true\"}", MediaType.APPLICATION_JSON));
        assertThrows(CepNaoEncontradoException.class, () -> service.consultar("99999999"));
    }

    @Test
    @DisplayName("Formato inválido lança CepInvalidoException sem chamar a API externa")
    void formatoInvalido() {
        assertThrows(CepInvalidoException.class, () -> service.consultar("123"));
        assertThrows(CepInvalidoException.class, () -> service.consultar("abcdefgh"));
        assertThrows(CepInvalidoException.class, () -> service.consultar(null));
        server.verify();
    }

    @Test
    @DisplayName("Erro 5xx do ViaCEP vira CepIndisponivelException")
    void erroServidorExterno() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/")).andRespond(withServerError());
        assertThrows(CepIndisponivelException.class, () -> service.consultar("01001000"));
    }

    @Test
    @DisplayName("Falha de rede vira CepIndisponivelException")
    void falhaDeRede() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withException(new IOException("sem conexão")));
        assertThrows(CepIndisponivelException.class, () -> service.consultar("01001000"));
    }

    @Test
    @DisplayName("Resposta 400 do ViaCEP vira CepIndisponivelException")
    void respostaBadRequestExterna() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/")).andRespond(withBadRequest());
        assertThrows(CepIndisponivelException.class, () -> service.consultar("01001000"));
    }
}
