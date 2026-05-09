package com.idnaheim.lifem.password;

import com.idnaheim.lifem.utilities.AuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name="passwords")
@Getter
@Setter
public class PasswordEntity extends AuditingEntity implements Serializable {

    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String platform;

    private String username;

    private String password;

    private boolean hasMFA;

    private String remarks;

}