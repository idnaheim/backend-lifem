package com.idnaheim.lifem.expense;

import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumBaseFrequency;
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

    private BigDecimal amount;

    private String description;

    @JsonDeserialize(using = LocalDateToInstantDeserializer.class)
    private Instant paymentStartDate;

    @Enumerated(EnumType.STRING)
    private EnumBaseFrequency frequency;

    @Enumerated(EnumType.STRING)
    private EnumBaseCategory category;

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