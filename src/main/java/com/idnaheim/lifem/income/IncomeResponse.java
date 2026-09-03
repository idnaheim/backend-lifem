package com.idnaheim.lifem.income;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.idnaheim.lifem.enums.IncomeCategory;
import com.idnaheim.lifem.enums.IncomeFrequency;
import com.idnaheim.lifem.transaction.TransactionEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record IncomeResponse(
        long id,
        String name,
        String source,
        BigDecimal amount,
        IncomeCategory category,
        IncomeFrequency frequency,
        Long accountId,
        String accountName,
        @JsonProperty("active") boolean isActive,
        String remarks,
        String createdBy,
        LocalDateTime createdDate,
        String modifiedBy,
        LocalDateTime modifiedDate,
        List<TransactionEntity> transactions
) {
    public static IncomeResponse fromEntity(IncomeEntity entity) {
        Long accountId = entity.getAccount() != null ? entity.getAccount().getId() : null;
        String accountName = entity.getAccount() != null ? entity.getAccount().getName() : null;

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
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getModifiedBy(),
                entity.getModifiedDate(),
                entity.getTransactions()
        );
    }
}
