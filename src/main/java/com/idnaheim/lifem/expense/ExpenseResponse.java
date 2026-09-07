package com.idnaheim.lifem.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumBaseFrequency;
import com.idnaheim.lifem.transaction.TransactionResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record ExpenseResponse(
    long id,
    String name,
    EnumBaseFrequency frequency,
    BigDecimal amount,
    String description,
    Instant paymentStartDate,
    EnumBaseCategory category,
    @JsonProperty("fixedAmount") 
    boolean isFixedAmount,
    @JsonProperty("active") 
    boolean isActive,
    List<TransactionResponse> transactions,
    String createdBy,
    LocalDateTime createdDate,
    String modifiedBy,
    LocalDateTime modifiedDate
) {
    public static ExpenseResponse fromEntity(ExpenseEntity entity) {
        List<TransactionResponse> transactions = entity.getTransactions() != null
                ? entity.getTransactions().stream().map(TransactionResponse::fromEntity).toList()
                : Collections.emptyList();

        return new ExpenseResponse(
            entity.getId(),
            entity.getName(),
            entity.getFrequency(),
            entity.getAmount(),
            entity.getDescription(),
            entity.getPaymentStartDate(),
            entity.getCategory(),
            entity.isFixedAmount(),
            entity.isActive(),
            transactions,
            entity.getCreatedBy(),
            entity.getCreatedDate(),
            entity.getModifiedBy(),
            entity.getModifiedDate()
        );
    }
}
