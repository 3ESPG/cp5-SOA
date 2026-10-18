package br.com.fiap3espg.autoescola3espg.infra.documentation;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    /**
     * Esquema JWT "bearer-key". Os controllers protegidos usam @SecurityRequirement(name = "bearer-key");
     * /login e a documentação ficam públicos. Use o botão "Authorize" do Swagger UI com o token do /login.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(
                                "bearer-key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .info(new Info()
                        .title("Auto Escola 3ESPG")
                        .version("CP5")
                        .description("API REST para gerenciamento de instruções da Auto Escola 3ESPG (FIAP - SOA e Web Services - Checkpoint 5).")
                        .contact(new Contact()
                                .name("Auto Escola 3ESPG")
                                .email("autoescola3espg@email.com")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")
                        )
                );
    }
}
