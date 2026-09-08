package com.idnaheim.lifem.messaging;

import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumTransactionType;
import com.idnaheim.lifem.transaction.TransactionEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionEvent(
        String eventType,
        long id,
        String referenceNo,
        long accountId,
        String accountName,
        EnumTransactionType type,
        EnumBaseCategory category,
        BigDecimal amount,
        String remarks,
        LocalDateTime transactionDateTime,
        Long expenseId,
        Long incomeId
) {

    public static TransactionEvent of(String eventType, TransactionEntity entity) {
        return new TransactionEvent(
                eventType,
                entity.getId(),
                entity.getReferenceNo(),
                entity.getAccount() != null ? entity.getAccount().getId() : 0L,
                entity.getAccount() != null ? entity.getAccount().getName() : null,
                entity.getType(),
                entity.getCategory(),
                entity.getAmount(),
                entity.getRemarks(),
                entity.getTransactionDateTime(),
                entity.getExpense() != null ? entity.getExpense().getId() : null,
                entity.getIncome() != null ? entity.getIncome().getId() : null
        );
    }
}
