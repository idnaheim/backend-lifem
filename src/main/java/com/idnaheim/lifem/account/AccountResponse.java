package com.idnaheim.lifem.account;

import com.idnaheim.lifem.enums.AccountCategory;
import com.idnaheim.lifem.enums.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
    long id,
    String name,
    BigDecimal balance,
    AccountCategory category,
    AccountType type,
    String remarks,
    String createdBy,
    LocalDateTime createdDate,
    String modifiedBy,
    LocalDateTime modifiedDate
) {
    public static AccountResponse fromEntity(AccountEntity entity) {
        return new AccountResponse(
            entity.getId(),
            entity.getName(),
            entity.getBalance(),
            entity.getCategory(),
            entity.getType(),
            entity.getRemarks(),
            entity.getCreatedBy(),
            entity.getCreatedDate(),
            entity.getModifiedBy(),
            entity.getModifiedDate()
        );
    }
}
