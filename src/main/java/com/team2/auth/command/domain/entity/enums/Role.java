package com.team2.auth.command.domain.entity.enums;

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

    public static Role fromDbValue(String v) {
        for (Role r : values()) {
            if (r.dbValue.equals(v)) return r;
        }
        throw new IllegalArgumentException("Unknown role: " + v);
    }
}
