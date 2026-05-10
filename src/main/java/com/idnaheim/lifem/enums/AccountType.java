package com.idnaheim.lifem.enums;

public enum AccountType {

    SAVINGS("SAVINGS"),
    CHECKING("CHECKING"),
    CURRENT("CURRENT"),
    CREDIT("CREDIT"),
    INVESTMENT("INVESTMENT");

    private final String value;

    AccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
