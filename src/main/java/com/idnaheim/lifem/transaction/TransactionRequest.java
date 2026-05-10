package com.idnaheim.lifem.transaction;

import com.idnaheim.lifem.enums.TransactionCategory;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionRequest {

    private long accountId;
    private Long expenseId;
    private BigDecimal amount;
    private TransactionCategory category;
    private String remarks;

}
