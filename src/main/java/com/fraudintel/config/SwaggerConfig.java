package com.fraudintel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI fraudEngineOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("AI Fraud Intelligence Engine API")
                        .description("Retrieval-Augmented Generation (RAG) defense system utilizing local ONNX embeddings and Claude orchestration.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Jigesh Mehta")));
    }
}