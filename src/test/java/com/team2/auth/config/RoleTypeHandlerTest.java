package com.team2.auth.config;

import com.team2.auth.command.domain.entity.enums.Role;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleTypeHandlerTest {

    private final RoleTypeHandler handler = new RoleTypeHandler();

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    @Test
    @DisplayName("Role 파라미터를 PreparedStatement에 문자열로 설정한다")
    void setNonNullParameter() throws SQLException {
        // when
        handler.setNonNullParameter(ps, 1, Role.ADMIN, JdbcType.VARCHAR);

        // then
        verify(ps).setString(1, "admin");
    }

    @Test
    @DisplayName("ResultSet에서 컬럼명으로 Role을 조회한다")
    void getNullableResult_byColumnName() throws SQLException {
        // given
        given(rs.getString("user_role")).willReturn("sales");

        // when
        Role result = handler.getNullableResult(rs, "user_role");

        // then
        assertThat(result).isEqualTo(Role.SALES);
    }

    @Test
    @DisplayName("ResultSet에서 컬럼명으로 조회 시 null이면 null을 반환한다")
    void getNullableResult_byColumnName_null() throws SQLException {
        // given
        given(rs.getString("user_role")).willReturn(null);

        // when
        Role result = handler.getNullableResult(rs, "user_role");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("ResultSet에서 컬럼 인덱스로 Role을 조회한다")
    void getNullableResult_byColumnIndex() throws SQLException {
        // given
        given(rs.getString(1)).willReturn("production");

        // when
        Role result = handler.getNullableResult(rs, 1);

        // then
        assertThat(result).isEqualTo(Role.PRODUCTION);
    }

    @Test
    @DisplayName("ResultSet에서 컬럼 인덱스로 조회 시 null이면 null을 반환한다")
    void getNullableResult_byColumnIndex_null() throws SQLException {
        // given
        given(rs.getString(1)).willReturn(null);

        // when
        Role result = handler.getNullableResult(rs, 1);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("CallableStatement에서 컬럼 인덱스로 Role을 조회한다")
    void getNullableResult_byCallableStatement() throws SQLException {
        // given
        given(cs.getString(1)).willReturn("shipping");

        // when
        Role result = handler.getNullableResult(cs, 1);

        // then
        assertThat(result).isEqualTo(Role.SHIPPING);
    }

    @Test
    @DisplayName("CallableStatement에서 컬럼 인덱스로 조회 시 null이면 null을 반환한다")
    void getNullableResult_byCallableStatement_null() throws SQLException {
        // given
        given(cs.getString(1)).willReturn(null);

        // when
        Role result = handler.getNullableResult(cs, 1);

        // then
        assertThat(result).isNull();
    }
}
