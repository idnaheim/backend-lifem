package com.idnaheim.lifem.income;

import com.idnaheim.lifem.enums.IncomeCategory;
import com.idnaheim.lifem.enums.IncomeFrequency;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class IncomeRequest {

    private String name;
    private String source;
    private BigDecimal amount;
    private IncomeCategory category;
    private IncomeFrequency frequency;
    private Long accountId;
    private boolean isActive;
    private String remarks;

}
