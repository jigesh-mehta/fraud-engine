package com.fraudintel.controller;

import com.fraudintel.domain.FraudRiskLevel;
import com.fraudintel.domain.InvestigationRequest;
import com.fraudintel.domain.InvestigationResponse;
import com.fraudintel.service.FraudInvestigationService;
import com.fraudintel.observability.TraceContext;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
public class FraudInvestigationController {

    private static final String RESILIENCE_INSTANCE = "llmInvestigate";

    private final TraceContext traceContext;

    private final FraudInvestigationService investigationService;

    @PostMapping("/investigate")
    @CircuitBreaker(name = RESILIENCE_INSTANCE, fallbackMethod = "manualReviewFallback")
    @Retry(name = RESILIENCE_INSTANCE, fallbackMethod = "manualReviewFallback")
    public ResponseEntity<InvestigationResponse> investigateTransaction(@RequestBody InvestigationRequest request) {
        // transactionId (when present) is the trace id for logs, MDC, and the response; otherwise a UUID is generated.
        String traceId = traceContext.bind(request);

        try {
            log.info("Initiating defensive fraud investigation for account: {}", request.accountId());

            // Defensive Constraint: Missing data forces the "Sad Path"
            if (request.amount() == null || request.accountId() == null) {
                String missingNotes = formatMissingCriticalDataNotes(request);
                log.warn("Missing critical payload data ({}). Forcing MANUAL_REVIEW.", missingNotes);
                return ResponseEntity.ok(new InvestigationResponse(
                        traceId,
                        FraudRiskLevel.MANUAL_REVIEW,
                        missingNotes
                ));
            }

            // Execute Semantic RAG & Claude Orchestration
            InvestigationResponse decision = investigationService.investigate(request);

            return ResponseEntity.ok(decision);
        } finally {
            traceContext.clear();
        }
    }

    /**
     * Strict Fallback Protocol
     */
    public ResponseEntity<InvestigationResponse> manualReviewFallback(InvestigationRequest request, Throwable t) {
        String traceId = traceContext.bind(request);

        try {
            log.error("Investigation pipeline failed. Safely routing to MANUAL_REVIEW. Reason: {}", t.getMessage());
            return ResponseEntity.ok(new InvestigationResponse(
                    traceId,
                    FraudRiskLevel.MANUAL_REVIEW,
                    "Fallback triggered: System latency, exception, or LLM timeout."
            ));
        } finally {
            traceContext.clear();
        }
    }

    private static String formatMissingCriticalDataNotes(InvestigationRequest request) {
        List<String> missing = new ArrayList<>();
        if (request.accountId() == null) {
            missing.add("accountId");
        }
        if (request.amount() == null) {
            missing.add("amount");
        }
        return "Missing critical data: " + String.join(", ", missing);
    }
}
