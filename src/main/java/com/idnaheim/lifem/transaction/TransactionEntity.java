package com.idnaheim.lifem.transaction;

import com.idnaheim.lifem.account.AccountEntity;
import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumTransactionType;
import com.idnaheim.lifem.expense.ExpenseEntity;
import com.idnaheim.lifem.income.IncomeEntity;
import com.idnaheim.lifem.utilities.AuditingEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name="transactions")
public class TransactionEntity extends AuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "reference_no", length = 10)
    private String referenceNo;

    @ManyToOne
    private AccountEntity account;

    @ManyToOne
    @JsonIgnore
    private ExpenseEntity expense;

    @ManyToOne
    @JsonIgnore
    private IncomeEntity income;

    @Enumerated(EnumType.STRING)
    private EnumBaseCategory category;

    @Enumerated(EnumType.STRING)
    private EnumTransactionType type;

    private BigDecimal amount;

    private String remarks;

    private LocalDateTime transactionDateTime;

    @PrePersist
    private void generateReferenceNo() {
        if (this.referenceNo == null) {
            this.referenceNo = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
        }
    }

}
