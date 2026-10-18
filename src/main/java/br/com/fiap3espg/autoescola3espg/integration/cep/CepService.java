package br.com.fiap3espg.autoescola3espg.integration.cep;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.regex.Pattern;

/** Serviço responsável pela comunicação com a API externa ViaCEP. */
@Service
public class CepService {
    private static final Pattern FORMATO_CEP = Pattern.compile("\\d{5}-?\\d{3}");

    private final RestClient client;

    public CepService(RestClient viaCepRestClient) {
        this.client = viaCepRestClient;
    }

    public ConsultaCepResponse consultar(String cep) {
        if (cep == null || !FORMATO_CEP.matcher(cep).matches()) {
            throw new CepInvalidoException("CEP deve conter 8 dígitos numéricos (ex.: 01001000 ou 01001-000)");
        }
        String cepNormalizado = cep.replace("-", "");
        ConsultaCepResponse resposta;
        try {
            resposta = client.get()
                    .uri("/{cep}/json/", cepNormalizado)
                    .retrieve()
                    .body(ConsultaCepResponse.class);
        } catch (RestClientException ex) {
            throw new CepIndisponivelException("Falha ao consultar o serviço externo ViaCEP", ex);
        }
        if (resposta == null || resposta.indicaErro()) {
            throw new CepNaoEncontradoException("CEP não encontrado");
        }
        return resposta;
    }
}
