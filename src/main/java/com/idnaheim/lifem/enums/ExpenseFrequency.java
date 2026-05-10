package com.idnaheim.lifem.enums;

public enum ExpenseFrequency {

    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    QUARTERLY("QUARTERLY"),
    ONE_TIME("ONE_TIME");

    private final String value;

    ExpenseFrequency(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


}
