package com.idnaheim.lifem.transaction;

import com.idnaheim.lifem.enums.TransactionCategory;
import com.idnaheim.lifem.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
    long id,
    String referenceNo,
    long accountId,
    String accountName,
    TransactionCategory category,
    TransactionType type,
    BigDecimal amount,
    String remarks,
    String createdBy,
    LocalDateTime createdDate,
    String modifiedBy,
    LocalDateTime modifiedDate
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
            entity.getCreatedBy(),
            entity.getCreatedDate(),
            entity.getModifiedBy(),
            entity.getModifiedDate()
        );
    }
}
