package com.idnaheim.lifem.income;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.idnaheim.lifem.enums.IncomeCategory;
import com.idnaheim.lifem.enums.IncomeFrequency;

import java.math.BigDecimal;

public record IncomeRequest(
        String name,
        String source,
        BigDecimal amount,
        IncomeCategory category,
        IncomeFrequency frequency,
        Long accountId,
        @JsonProperty("active") boolean isActive,
        String remarks
) {}
