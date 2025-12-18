package com.tickettriage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Customer Support Ticket Triage API")
                        .version("1.0.0")
                        .description("REST API for automated ticket classification using Google Gemini AI. " +
                                "This system provides asynchronous ticket processing with AI-powered categorization, " +
                                "priority assignment, and sentiment analysis.")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@tickettriage.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")));
    }
}
