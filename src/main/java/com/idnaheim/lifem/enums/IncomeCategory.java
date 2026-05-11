package com.idnaheim.lifem.enums;

public enum IncomeCategory {

    SALARY("SALARY"),
    FREELANCE("FREELANCE"),
    BUSINESS("BUSINESS"),
    INVESTMENT("INVESTMENT"),
    RENTAL("RENTAL"),
    DIVIDEND("DIVIDEND"),
    INTEREST("INTEREST"),
    SIDE_HUSTLE("SIDE_HUSTLE"),
    OTHER("OTHER");

    private final String value;

    IncomeCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
