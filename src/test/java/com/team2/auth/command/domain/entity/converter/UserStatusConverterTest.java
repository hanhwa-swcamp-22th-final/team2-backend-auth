package com.team2.auth.command.domain.entity.converter;

import com.team2.auth.command.domain.entity.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserStatusConverterTest {

    private final UserStatusConverter converter = new UserStatusConverter();

    @Test
    @DisplayName("UserStatus를 DB 문자열로 변환한다")
    void convertToDatabaseColumn_allValues() {
        assertThat(converter.convertToDatabaseColumn(UserStatus.ACTIVE)).isEqualTo("active");
        assertThat(converter.convertToDatabaseColumn(UserStatus.ON_LEAVE)).isEqualTo("on_leave");
        assertThat(converter.convertToDatabaseColumn(UserStatus.RETIRED)).isEqualTo("retired");
    }

    @Test
    @DisplayName("null UserStatus는 null로 변환한다")
    void convertToDatabaseColumn_null() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    @DisplayName("DB 문자열을 UserStatus로 변환한다")
    void convertToEntityAttribute_allValues() {
        assertThat(converter.convertToEntityAttribute("active")).isEqualTo(UserStatus.ACTIVE);
        assertThat(converter.convertToEntityAttribute("on_leave")).isEqualTo(UserStatus.ON_LEAVE);
        assertThat(converter.convertToEntityAttribute("retired")).isEqualTo(UserStatus.RETIRED);
    }

    @Test
    @DisplayName("null 문자열은 null UserStatus로 변환한다")
    void convertToEntityAttribute_null() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
