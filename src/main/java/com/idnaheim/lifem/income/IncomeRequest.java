package com.idnaheim.lifem.income;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumBaseFrequency;

import java.math.BigDecimal;

public record IncomeRequest(
        String name,
        String source,
        BigDecimal amount,
        EnumBaseCategory category,
        EnumBaseFrequency frequency,
        Long accountId,
        @JsonProperty("active") boolean isActive,
        String remarks
) {}
