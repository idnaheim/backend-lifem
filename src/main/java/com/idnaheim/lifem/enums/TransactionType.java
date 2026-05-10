package com.idnaheim.lifem.enums;

public enum TransactionType {

    INCOME("INCOME"),
    EXPENSE("EXPENSE"),
    TRANSFER("TRANSFER");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
