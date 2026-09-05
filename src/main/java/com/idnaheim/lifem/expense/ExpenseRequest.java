package com.idnaheim.lifem.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.idnaheim.lifem.config.LocalDateToInstantDeserializer;
import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumBaseFrequency;

import java.math.BigDecimal;
import java.time.Instant;

public record ExpenseRequest(
        String name,
        EnumBaseFrequency frequency,
        BigDecimal amount,
        String description,
        @JsonDeserialize(using = LocalDateToInstantDeserializer.class)
        Instant paymentStartDate,
        EnumBaseCategory category,
        @JsonProperty("fixedAmount") Boolean isFixedAmount,
        @JsonProperty("active") Boolean isActive
) {
    public ExpenseEntity toEntity() {
        ExpenseEntity entity = new ExpenseEntity();
        entity.setName(name);
        entity.setFrequency(frequency);
        entity.setAmount(amount);
        entity.setDescription(description);
        entity.setPaymentStartDate(paymentStartDate);
        entity.setCategory(category);
        entity.setFixedAmount(isFixedAmount != null && isFixedAmount);
        entity.setActive(isActive == null || isActive);
        return entity;
    }
}
