package com.idnaheim.lifem.income;

import com.idnaheim.lifem.enums.IncomeCategory;
import com.idnaheim.lifem.enums.IncomeFrequency;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class IncomeResponse {

    private long id;
    private String name;
    private String source;
    private BigDecimal amount;
    private IncomeCategory category;
    private IncomeFrequency frequency;
    private Long accountId;
    private String accountName;
    private boolean isActive;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;

    public static IncomeResponse fromEntity(IncomeEntity entity) {
        IncomeResponse response = new IncomeResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setSource(entity.getSource());
        response.setAmount(entity.getAmount());
        response.setCategory(entity.getCategory());
        response.setFrequency(entity.getFrequency());
        response.setActive(entity.isActive());
        response.setRemarks(entity.getRemarks());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setModifiedBy(entity.getModifiedBy());
        response.setModifiedDate(entity.getModifiedDate());

        if (entity.getAccount() != null) {
            response.setAccountId(entity.getAccount().getId());
            response.setAccountName(entity.getAccount().getName());
        }

        return response;
    }

}
