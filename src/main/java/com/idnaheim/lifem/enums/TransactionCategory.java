package com.idnaheim.lifem.enums;

public enum TransactionCategory {

    HOUSING("HOUSING"),
    CAR("CAR"),
    FOOD("FOOD"),
    OFFICE("OFFICE"),
    TRAVEL("TRAVEL"),
    LEISURE("LEISURE"),
    SHOPPING("SHOPPING"),
    HEALTH("HEALTH"),
    SALARY("SALARY"),
    INVESTMENT("INVESTMENT"),
    OTHER("OTHER"),
    TRANSFER("TRANSFER");

    private final String value;

    TransactionCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
