package com.team2.auth.command.domain.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    ADMIN("admin"),
    SALES("sales"),
    PRODUCTION("production"),
    SHIPPING("shipping");

    private final String dbValue;

    Role(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static Role fromJson(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(normalized) || role.dbValue.equalsIgnoreCase(normalized)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Unknown role: " + value);
    }

    public static Role fromDbValue(String v) {
        for (Role r : values()) {
            if (r.dbValue.equals(v)) return r;
        }
        throw new IllegalArgumentException("Unknown role: " + v);
    }
}
