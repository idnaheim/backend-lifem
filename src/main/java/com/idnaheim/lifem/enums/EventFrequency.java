package com.idnaheim.lifem.enums;

public enum EventFrequency {

    ONCE("ONCE"),
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    YEARLY("YEARLY");

    private final String value;

    EventFrequency(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
