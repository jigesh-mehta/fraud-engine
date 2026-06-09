package com.fraudintel.service;

import com.fraudintel.domain.InvestigationRequest;
import com.fraudintel.domain.InvestigationResponse;
import com.fraudintel.repository.FraudVectorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FraudInvestigationService {

    private final ChatClient chatClient;
    private final FraudVectorRepository vectorRepository;

    public FraudInvestigationService(ChatClient.Builder chatClientBuilder, FraudVectorRepository vectorRepository) {
        this.chatClient = chatClientBuilder.build();
        this.vectorRepository = vectorRepository;
    }

    public InvestigationResponse investigate(InvestigationRequest request) {
        // 1. Build the semantic search query
        String transactionDetails = String.format("Transaction Amount: $%.2f, Merchant Category Code: %s",
                request.amount(), request.merchantCategoryCode());

        log.info("Executing native pgvector similarity search for historical context...");

        // 2. Retrieve Top 2 Similar Cases
        List<String> similarCases = vectorRepository.findSimilarHistoricalCases(transactionDetails, 2);
        String historicalContext = String.join("\n", similarCases);
        
        log.info("Retrieved contextual intelligence from Supabase.");

        // 3. Define the Structured Output Enforcer
        BeanOutputConverter<InvestigationResponse> converter = new BeanOutputConverter<>(InvestigationResponse.class);

        // 4. Construct the Guardrailed System Prompt
        String systemPrompt = """
                You are a defensive AI Fraud Intelligence Oracle.
                Analyze the incoming transaction against the provided HISTORICAL CONTEXT.
                Determine if the transaction exhibits similarities to known threat scenarios.
                
                HISTORICAL CONTEXT:
                {historical_context}
                
                You must strictly output your response as JSON matching the requested schema.
                {format}
                """;

        log.info("Orchestrating Claude 3.5 Sonnet for advisory decision...");

        // 5. Execute the LLM Call
        InvestigationResponse generatedResponse = chatClient.prompt()
                .system(s -> s.text(systemPrompt)
                        .param("historical_context", historicalContext)
                        .param("format", converter.getFormat()))
                .user(transactionDetails)
                .call()
                .entity(converter);

        // Map the generated reasoning back to the original request ID
        return new InvestigationResponse(
                request.transactionId(),
                generatedResponse.riskLevel(),
                generatedResponse.advisoryNotes()
        );
    }
}