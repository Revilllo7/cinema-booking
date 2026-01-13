package com.cinema.entity;

import java.util.Arrays;

public enum TicketTypeName {
    STANDARD("Standard"),
    STUDENT("Student"),
    SENIOR("Senior"),
    CHILD("Child");

    private final String dbName;

    TicketTypeName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }

    public static TicketTypeName fromString(String value) {
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(value) || type.dbName.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported ticket type: " + value));
    }
}
