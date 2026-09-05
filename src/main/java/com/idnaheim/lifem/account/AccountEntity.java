package com.idnaheim.lifem.account;

import com.idnaheim.lifem.enums.EnumAccountCategory;
import com.idnaheim.lifem.enums.EnumAccountType;
import com.idnaheim.lifem.utilities.AuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


@Getter
@Setter
@Entity
@Table(name="accounts")
public class AccountEntity extends AuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private EnumAccountCategory category;

    @Enumerated(EnumType.STRING)
    private EnumAccountType type;

    private String remarks;



}
