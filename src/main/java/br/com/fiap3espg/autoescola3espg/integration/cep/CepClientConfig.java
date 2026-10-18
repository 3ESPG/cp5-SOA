package br.com.fiap3espg.autoescola3espg.integration.cep;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/** Configura o cliente HTTP (RestClient) usado para chamar o ViaCEP, com timeouts. */
@Configuration
public class CepClientConfig {

    @Bean
    public RestClient viaCepRestClient(@Value("${integracao.viacep.url:https://viacep.com.br/ws}") String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));
        // RestClient.builder() estático: não depende de autoconfiguração do Spring Boot 4.
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }
}
