package com.idnaheim.lifem.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequest {

    private long fromAccountId;
    private long toAccountId;
    private double amount;

}
