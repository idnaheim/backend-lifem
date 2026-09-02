package com.idnaheim.lifem.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.idnaheim.lifem.enums.ExpenseCategory;
import com.idnaheim.lifem.enums.ExpenseFrequency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record ExpenseResponse(
    long id,
    String name,
    ExpenseFrequency frequency,
    BigDecimal amount,
    String description,
    Instant paymentStartDate,
    ExpenseCategory category,
    @JsonProperty("fixedAmount") 
    boolean isFixedAmount,
    @JsonProperty("active") 
    boolean isActive,
    @JsonProperty("paid") 
    boolean isPaid,
    long missedPayments,
    String createdBy,
    LocalDateTime createdDate,
    String modifiedBy,
    LocalDateTime modifiedDate
) {
    public static ExpenseResponse fromEntity(ExpenseEntity entity) {
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
            entity.isPaid(),
            entity.getMissedPayments(),
            entity.getCreatedBy(),
            entity.getCreatedDate(),
            entity.getModifiedBy(),
            entity.getModifiedDate()
        );
    }
}
