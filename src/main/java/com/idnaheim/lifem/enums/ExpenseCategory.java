package com.idnaheim.lifem.enums;

public enum ExpenseCategory {

    HOUSING("HOUSING"),
    CAR("CAR"),
    FOOD("FOOD"),
    OFFICE("OFFICE"),
    LEISURE("LEISURE"),
    HEALTH("HEALTH");

    private final String value;

    ExpenseCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
