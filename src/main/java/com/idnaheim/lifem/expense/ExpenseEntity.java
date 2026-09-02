package com.idnaheim.lifem.expense;

import com.idnaheim.lifem.enums.ExpenseCategory;
import com.idnaheim.lifem.enums.ExpenseFrequency;
import com.idnaheim.lifem.transaction.TransactionEntity;
import com.idnaheim.lifem.utilities.AuditingEntity;
import com.idnaheim.lifem.config.LocalDateToInstantDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name="expenses")
public class ExpenseEntity extends AuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ExpenseFrequency frequency;

    private BigDecimal amount;

    private String description;

    @JsonDeserialize(using = LocalDateToInstantDeserializer.class)
    private Instant paymentStartDate;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    private boolean isFixedAmount;

    @Column(columnDefinition = "boolean default true")
    private boolean isActive = true;

    @Transient
    private boolean isPaid;

    @Transient
    private long missedPayments;

    @Transient
    private List<TransactionEntity> transactions;

}