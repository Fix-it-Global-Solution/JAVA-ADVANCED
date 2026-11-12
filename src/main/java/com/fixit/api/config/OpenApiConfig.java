package com.fixit.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication (JWT)";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fix-It API — Plataforma Inteligente de Zeladoria Urbana (CitizenTech)")
                        .version("1.0.0")
                        .description("""
                                API RESTful da plataforma CitizenTech **Fix-It** (Global Solution FIAP — Java Advanced).
                                
                                **Destaques Arquiteturais:**
                                - **Segurança Stateless (Spring Security 6 + JWT):** Controle de acesso baseado em papéis (RBAC) segregando Cidadãos (`ROLE_CIDADAO`) e Gestores Públicos (`ROLE_GESTOR`).
                                - **Inteligência Artificial Generativa (Spring AI + Google Gemini):** Triagem técnica automatizada e geração de laudos de engenharia urbana utilizando o modelo gratuito **`gemini-1.5-flash`** via **Google AI Studio** (`ChatModel` / `ChatClient`).
                                - **Arquitetura Distribuída Assíncrona (RabbitMQ):** Publicação e consumo assíncrono de eventos de triagem na fila `fila-triagem-zeladoria` com tolerância a falhas.
                                """)
                        .contact(new Contact()
                                .name("Equipe Fix-It — Global Solution FIAP")
                                .url("https://github.com/Fix-it-Global-Solution/JAVA-ADVANCED"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT obtido no endpoint `/api/v1/auth/login` (sem o prefixo 'Bearer ').")));
    }

}

