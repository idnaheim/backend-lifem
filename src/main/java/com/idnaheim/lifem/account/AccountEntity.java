package com.idnaheim.lifem.account;

import com.idnaheim.lifem.enums.AccountCategory;
import com.idnaheim.lifem.enums.AccountType;
import com.idnaheim.lifem.utilities.AuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;


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

    private double balance;

    @Enumerated(EnumType.STRING)
    private AccountCategory category;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    private String remarks;



}
