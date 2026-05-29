package com.fraudintel.domain;

public record InvestigationResponse(
    String transactionId,
    FraudRiskLevel riskLevel,
    String advisoryNotes
) {}
