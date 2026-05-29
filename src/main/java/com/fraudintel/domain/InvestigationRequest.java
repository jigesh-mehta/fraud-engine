package com.fraudintel.domain;

public record InvestigationRequest(
    String transactionId,
    String accountId,
    Double amount,
    String merchantCategoryCode
) {}
