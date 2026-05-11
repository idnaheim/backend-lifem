package com.idnaheim.lifem.enums;

public enum EventCategory {

    PERSONAL("PERSONAL"),
    WORK("WORK"),
    HEALTH("HEALTH"),
    FINANCE("FINANCE"),
    TRAVEL("TRAVEL"),
    OTHER("OTHER");

    private final String value;

    EventCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
