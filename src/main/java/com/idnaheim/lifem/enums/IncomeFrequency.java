package com.idnaheim.lifem.enums;

public enum IncomeFrequency {

    WEEKLY("WEEKLY"),
    BI_WEEKLY("BI_WEEKLY"),
    MONTHLY("MONTHLY"),
    QUARTERLY("QUARTERLY"),
    YEARLY("YEARLY"),
    ONE_TIME("ONE_TIME");

    private final String value;

    IncomeFrequency(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
