package com.idnaheim.lifem.enums;

public enum AccountCategory {

    BANK("BANK"),
    CASH("CASH"),
    E_WALLET("E_WALLET");

    private final String value;

    AccountCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
