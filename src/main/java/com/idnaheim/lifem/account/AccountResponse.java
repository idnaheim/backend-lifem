package com.idnaheim.lifem.account;

import com.idnaheim.lifem.enums.EnumAccountCategory;
import com.idnaheim.lifem.enums.EnumAccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
    long id,
    String name,
    BigDecimal balance,
    EnumAccountCategory category,
    EnumAccountType type,
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
