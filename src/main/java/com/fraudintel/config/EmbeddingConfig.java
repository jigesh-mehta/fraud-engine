package com.fraudintel.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class EmbeddingConfig {

    @Bean
    @Primary
    public EmbeddingModel fraudEmbeddingModel(
            @Value("${spring.ai.embedding.transformer.tokenizer.uri}") String tokenizerUri,
            @Value("${spring.ai.embedding.transformer.onnx.model-uri}") String modelUri) throws Exception {
        TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();
        embeddingModel.setTokenizerResource(tokenizerUri);
        embeddingModel.setModelResource(modelUri);
        embeddingModel.afterPropertiesSet();
        return embeddingModel;
    }
}
