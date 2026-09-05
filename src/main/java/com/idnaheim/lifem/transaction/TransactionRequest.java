package com.idnaheim.lifem.transaction;

import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumTransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionRequest {

    private long accountId;
    private Long expenseId;
    private Long incomeId;
    private BigDecimal amount;
    private EnumBaseCategory category;
    private EnumTransactionType type;
    private String remarks;
    private LocalDateTime transactionDateTime;

}
