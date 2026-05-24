package com.idnaheim.lifem.income;

import com.idnaheim.lifem.account.AccountEntity;
import com.idnaheim.lifem.enums.IncomeCategory;
import com.idnaheim.lifem.enums.IncomeFrequency;
import com.idnaheim.lifem.transaction.TransactionEntity;
import com.idnaheim.lifem.utilities.AuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "incomes")
public class IncomeEntity extends AuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String source;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private IncomeCategory category;

    @Enumerated(EnumType.STRING)
    private IncomeFrequency frequency;

    @ManyToOne
    private AccountEntity account;

    private boolean isActive;

    private String remarks;

    @Transient
    private List<TransactionEntity> transactions;

}
