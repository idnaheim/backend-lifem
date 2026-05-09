package com.idnaheim.lifem.expense;

import com.idnaheim.lifem.common.ExpenseCategory;
import com.idnaheim.lifem.common.ExpenseFrequency;
import com.idnaheim.lifem.utilities.AuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name="expenses")
public class ExpenseEntity extends AuditingEntity implements Serializable {

    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private ExpenseFrequency frequency;

    private BigDecimal amount;

    private String description;

    private Instant paymentStartDate;

    @Transient
    private boolean isPaid;

    private ExpenseCategory category;

}