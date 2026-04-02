package com.team2.auth.config;

import com.team2.auth.command.domain.entity.enums.UserStatus;
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
class UserStatusTypeHandlerTest {

    private final UserStatusTypeHandler handler = new UserStatusTypeHandler();

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    @Test
    @DisplayName("UserStatus 파라미터를 PreparedStatement에 문자열로 설정한다")
    void setNonNullParameter() throws SQLException {
        // when
        handler.setNonNullParameter(ps, 1, UserStatus.ACTIVE, JdbcType.VARCHAR);

        // then
        verify(ps).setString(1, "active");
    }

    @Test
    @DisplayName("ResultSet에서 컬럼명으로 UserStatus를 조회한다")
    void getNullableResult_byColumnName() throws SQLException {
        // given
        given(rs.getString("user_status")).willReturn("active");

        // when
        UserStatus result = handler.getNullableResult(rs, "user_status");

        // then
        assertThat(result).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("ResultSet에서 컬럼명으로 조회 시 null이면 null을 반환한다")
    void getNullableResult_byColumnName_null() throws SQLException {
        // given
        given(rs.getString("user_status")).willReturn(null);

        // when
        UserStatus result = handler.getNullableResult(rs, "user_status");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("ResultSet에서 컬럼 인덱스로 UserStatus를 조회한다")
    void getNullableResult_byColumnIndex() throws SQLException {
        // given
        given(rs.getString(1)).willReturn("on_leave");

        // when
        UserStatus result = handler.getNullableResult(rs, 1);

        // then
        assertThat(result).isEqualTo(UserStatus.ON_LEAVE);
    }

    @Test
    @DisplayName("ResultSet에서 컬럼 인덱스로 조회 시 null이면 null을 반환한다")
    void getNullableResult_byColumnIndex_null() throws SQLException {
        // given
        given(rs.getString(1)).willReturn(null);

        // when
        UserStatus result = handler.getNullableResult(rs, 1);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("CallableStatement에서 컬럼 인덱스로 UserStatus를 조회한다")
    void getNullableResult_byCallableStatement() throws SQLException {
        // given
        given(cs.getString(1)).willReturn("retired");

        // when
        UserStatus result = handler.getNullableResult(cs, 1);

        // then
        assertThat(result).isEqualTo(UserStatus.RETIRED);
    }

    @Test
    @DisplayName("CallableStatement에서 컬럼 인덱스로 조회 시 null이면 null을 반환한다")
    void getNullableResult_byCallableStatement_null() throws SQLException {
        // given
        given(cs.getString(1)).willReturn(null);

        // when
        UserStatus result = handler.getNullableResult(cs, 1);

        // then
        assertThat(result).isNull();
    }
}
