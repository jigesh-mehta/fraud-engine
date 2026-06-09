package com.fraudintel.repository;

import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FraudVectorRepository {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;

    public List<String> findSimilarHistoricalCases(String query, int topK) {
        log.debug("Embedding RAG query natively via ONNX...");
        float[] queryVector = toFloatArray(embeddingModel.embed(query));

        // The <=> operator calculates Cosine Distance natively in pgvector
        String sql = """
                SELECT threat_scenario, advisory_context, risk_level, 
                       1 - (embedding <=> ?) as similarity
                FROM synthetic_fraud_cases
                ORDER BY embedding <=> ?
                LIMIT ?
                """;

        return jdbcTemplate.query(sql,
                ps -> {
                    ps.setObject(1, new PGvector(queryVector));
                    ps.setObject(2, new PGvector(queryVector));
                    ps.setInt(3, topK);
                },
                (rs, rowNum) -> String.format(
                        "[HISTORICAL CASE %d] Scenario: %s | Known Risk: %s | Context: %s (Cosine Similarity: %.2f)",
                        rowNum + 1,
                        rs.getString("threat_scenario"),
                        rs.getString("risk_level"),
                        rs.getString("advisory_context"),
                        rs.getFloat("similarity")
                )
        );
    }

    private static float[] toFloatArray(List<Double> embedding) {
        float[] vector = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            vector[i] = embedding.get(i).floatValue();
        }
        return vector;
    }
}