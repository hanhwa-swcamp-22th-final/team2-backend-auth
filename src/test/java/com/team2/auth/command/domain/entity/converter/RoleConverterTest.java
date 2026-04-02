package com.team2.auth.command.domain.entity.converter;

import com.team2.auth.command.domain.entity.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleConverterTest {

    private final RoleConverter converter = new RoleConverter();

    @Test
    @DisplayName("Role을 DB 문자열로 변환한다")
    void convertToDatabaseColumn_allValues() {
        assertThat(converter.convertToDatabaseColumn(Role.ADMIN)).isEqualTo("admin");
        assertThat(converter.convertToDatabaseColumn(Role.SALES)).isEqualTo("sales");
        assertThat(converter.convertToDatabaseColumn(Role.PRODUCTION)).isEqualTo("production");
        assertThat(converter.convertToDatabaseColumn(Role.SHIPPING)).isEqualTo("shipping");
    }

    @Test
    @DisplayName("null Role은 null로 변환한다")
    void convertToDatabaseColumn_null() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    @DisplayName("DB 문자열을 Role로 변환한다")
    void convertToEntityAttribute_allValues() {
        assertThat(converter.convertToEntityAttribute("admin")).isEqualTo(Role.ADMIN);
        assertThat(converter.convertToEntityAttribute("sales")).isEqualTo(Role.SALES);
        assertThat(converter.convertToEntityAttribute("production")).isEqualTo(Role.PRODUCTION);
        assertThat(converter.convertToEntityAttribute("shipping")).isEqualTo(Role.SHIPPING);
    }

    @Test
    @DisplayName("null 문자열은 null Role로 변환한다")
    void convertToEntityAttribute_null() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
