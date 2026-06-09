package com.fraudintel.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyntheticFraudCaseSeedService {

    private static final String INSERT_SQL = """
            INSERT INTO synthetic_fraud_cases (threat_scenario, advisory_context, risk_level, embedding)
            VALUES (?, ?, ?, ?)
            """;

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Value("classpath:data/synthetic-fraud-seed.json")
    private Resource seedDataResource;

    @Value("${fraud.seed.force:false}")
    private boolean forceSeed;

    public void seed() {
        if (!forceSeed && tableAlreadySeeded()) {
            log.info("synthetic_fraud_cases already contains data; skipping seed (set fraud.seed.force=true to re-seed).");
            return;
        }

        if (forceSeed) {
            log.warn("fraud.seed.force=true — truncating synthetic_fraud_cases before seeding.");
            jdbcTemplate.execute("TRUNCATE TABLE synthetic_fraud_cases");
        }

        List<SyntheticFraudCaseSeed> seeds = loadSeedData();
        log.info("Seeding {} synthetic fraud cases with MiniLM embeddings (384 dimensions).", seeds.size());

        for (SyntheticFraudCaseSeed seed : seeds) {
            String embedText = buildEmbedText(seed);
            float[] vector = toFloatArray(embeddingModel.embed(embedText));
            if (vector.length != 384) {
                throw new IllegalStateException(
                        "Expected 384-dimensional embedding but got " + vector.length);
            }

            jdbcTemplate.update(
                    INSERT_SQL,
                    seed.threatScenario(),
                    seed.advisoryContext(),
                    seed.riskLevel(),
                    new PGvector(vector)
            );
            log.debug("Seeded threat scenario: {}", seed.threatScenario());
        }

        log.info("Successfully seeded synthetic_fraud_cases.");
    }

    private boolean tableAlreadySeeded() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM synthetic_fraud_cases",
                Integer.class
        );
        return count != null && count > 0;
    }

    private List<SyntheticFraudCaseSeed> loadSeedData() {
        try (InputStream inputStream = seedDataResource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load synthetic fraud seed data", ex);
        }
    }

    private static String buildEmbedText(SyntheticFraudCaseSeed seed) {
        return seed.threatScenario() + ": " + seed.advisoryContext();
    }

    private static float[] toFloatArray(List<Double> embedding) {
        float[] vector = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            vector[i] = embedding.get(i).floatValue();
        }
        return vector;
    }
}
