package com.idnaheim.lifem.transaction;

import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumTransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
    long id,
    String referenceNo,
    long accountId,
    String accountName,
    EnumBaseCategory category,
    EnumTransactionType type,
    BigDecimal amount,
    String remarks,
    LocalDateTime transactionDateTime,
    String createdBy,
    LocalDateTime createdDate,
    String modifiedBy,
    LocalDateTime modifiedDate,
    long expenseId,
    String expenseName,
    long incomeId,
    String incomeName
) {
    public static TransactionResponse fromEntity(TransactionEntity entity) {
        return new TransactionResponse(
            entity.getId(),
            entity.getReferenceNo(),
            entity.getAccount() != null ? entity.getAccount().getId() : 0,
            entity.getAccount() != null ? entity.getAccount().getName() : null,
            entity.getCategory(),
            entity.getType(),
            entity.getAmount(),
            entity.getRemarks(),
            entity.getTransactionDateTime(),
            entity.getCreatedBy(),
            entity.getCreatedDate(),
            entity.getModifiedBy(),
            entity.getModifiedDate(),
            entity.getExpense() != null ? entity.getExpense().getId() : 0,
            entity.getExpense() != null ? entity.getExpense().getName() : null,
            entity.getIncome() != null ? entity.getIncome().getId() : 0,
            entity.getIncome() != null ? entity.getIncome().getName() : null
        );
    }
}
