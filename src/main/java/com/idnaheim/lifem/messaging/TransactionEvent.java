package com.idnaheim.lifem.messaging;

import com.idnaheim.lifem.enums.TransactionCategory;
import com.idnaheim.lifem.enums.TransactionType;
import com.idnaheim.lifem.transaction.TransactionEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionEvent(
        long id,
        String referenceNo,
        long accountId,
        String accountName,
        TransactionCategory category,
        TransactionType type,
        BigDecimal amount,
        String remarks,
        String createdBy,
        LocalDateTime createdDate
) {
    public static TransactionEvent fromEntity(TransactionEntity entity) {
        return new TransactionEvent(
                entity.getId(),
                entity.getReferenceNo(),
                entity.getAccount() != null ? entity.getAccount().getId() : 0,
                entity.getAccount() != null ? entity.getAccount().getName() : null,
                entity.getCategory(),
                entity.getType(),
                entity.getAmount(),
                entity.getRemarks(),
                entity.getCreatedBy(),
                entity.getCreatedDate()
        );
    }
}
