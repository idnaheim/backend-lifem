package com.idnaheim.lifem.account;

import java.math.BigDecimal;

public record TransferRequest(
        long fromAccountId,
        long toAccountId,
        BigDecimal amount
) {}
