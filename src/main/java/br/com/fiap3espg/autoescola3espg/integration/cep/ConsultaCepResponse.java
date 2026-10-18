package br.com.fiap3espg.autoescola3espg.integration.cep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Dados retornados pelo serviço público ViaCEP (https://viacep.com.br).
 * O campo "erro" é lido como Object porque o ViaCEP pode devolver true ou "true".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConsultaCepResponse(String cep, String logradouro, String complemento,
                                  String bairro, String localidade, String uf, Object erro) {
    public boolean indicaErro() {
        return erro != null && "true".equalsIgnoreCase(String.valueOf(erro));
    }
}
