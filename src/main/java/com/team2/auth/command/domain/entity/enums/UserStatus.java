package com.team2.auth.command.domain.entity.enums;

public enum UserStatus {
    ACTIVE("active"),       // 재직
    ON_LEAVE("on_leave"),   // 휴직
    RETIRED("retired");     // 퇴직 (터미널 상태)

    private final String dbValue;

    UserStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static UserStatus fromDbValue(String v) {
        for (UserStatus s : values()) {
            if (s.dbValue.equals(v)) return s;
        }
        throw new IllegalArgumentException("Unknown user status: " + v);
    }
}
