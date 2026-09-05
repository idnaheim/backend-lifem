package com.idnaheim.lifem.income;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumBaseFrequency;
import com.idnaheim.lifem.transaction.TransactionResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record IncomeResponse(
        long id,
        String name,
        String source,
        BigDecimal amount,
        EnumBaseCategory category,
        EnumBaseFrequency frequency,
        Long accountId,
        String accountName,
        @JsonProperty("active") boolean isActive,
        String remarks,
        @JsonProperty("received") boolean isReceived,
        long missedPayments,
        List<TransactionResponse> transactions,
        String createdBy,
        LocalDateTime createdDate,
        String modifiedBy,
        LocalDateTime modifiedDate
) {
    public static IncomeResponse fromEntity(IncomeEntity entity) {
        Long accountId = entity.getAccount() != null ? entity.getAccount().getId() : null;
        String accountName = entity.getAccount() != null ? entity.getAccount().getName() : null;

        List<TransactionResponse> transactions = entity.getTransactions() != null
                ? entity.getTransactions().stream().map(TransactionResponse::fromEntity).toList()
                : Collections.emptyList();

        return new IncomeResponse(
                entity.getId(),
                entity.getName(),
                entity.getSource(),
                entity.getAmount(),
                entity.getCategory(),
                entity.getFrequency(),
                accountId,
                accountName,
                entity.isActive(),
                entity.getRemarks(),
                entity.isReceived(),
                entity.getMissedPayments(),
                transactions,
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getModifiedBy(),
                entity.getModifiedDate()
        );
    }
}
