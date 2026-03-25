package com.team2.auth.security;

import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private static final String SECRET = "testSecretKeyForJwtTestingPurposesMustBe256BitsLongEnough!!";
    private static final long ACCESS_TOKEN_EXPIRY = 3600000L;
    private static final long REFRESH_TOKEN_EXPIRY = 604800000L;

    private JwtProvider jwtProvider;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        jwtProvider = new JwtProvider(SECRET, ACCESS_TOKEN_EXPIRY, REFRESH_TOKEN_EXPIRY);

        testUser = User.builder()
                .employeeNo("EMP001")
                .name("홍길동")
                .email("hong@test.com")
                .pw("encodedPassword")
                .role(Role.SALES)
                .status(UserStatus.재직)
                .build();

        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testUser, 1);
    }

    @Test
    @DisplayName("AccessToken 생성 시 null이 아니고 빈 문자열이 아니다")
    void generateAccessToken_returnsNonEmptyToken() {
        // when
        String token = jwtProvider.generateAccessToken(testUser);

        // then
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("AccessToken 파싱 시 subject와 claim이 정확하다")
    void parseAccessToken_returnsCorrectClaims() {
        // given
        String token = jwtProvider.generateAccessToken(testUser);

        // when
        Claims claims = jwtProvider.parseAccessToken(token);

        // then
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("email", String.class)).isEqualTo("hong@test.com");
        assertThat(claims.get("name", String.class)).isEqualTo("홍길동");
        assertThat(claims.get("role", String.class)).isEqualTo("SALES");
    }

    @Test
    @DisplayName("RefreshToken 생성 시 UUID 형식이다")
    void generateRefreshToken_returnsUuidFormat() {
        // when
        String refreshToken = jwtProvider.generateRefreshToken();

        // then
        assertThat(refreshToken).matches(
                "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
        );
    }

    @Test
    @DisplayName("유효한 토큰 검증 시 true를 반환한다")
    void validateAccessToken_withValidToken_returnsTrue() {
        // given
        String token = jwtProvider.generateAccessToken(testUser);

        // when
        boolean result = jwtProvider.validateAccessToken(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("변조된 토큰 검증 시 false를 반환한다")
    void validateAccessToken_withTamperedToken_returnsFalse() {
        // given
        String token = jwtProvider.generateAccessToken(testUser);
        String tamperedToken = token + "tampered";

        // when
        boolean result = jwtProvider.validateAccessToken(tamperedToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 시 false를 반환한다")
    void validateAccessToken_withExpiredToken_returnsFalse() throws InterruptedException {
        // given
        JwtProvider shortLivedProvider = new JwtProvider(SECRET, 1L, REFRESH_TOKEN_EXPIRY);
        String token = shortLivedProvider.generateAccessToken(testUser);
        Thread.sleep(50);

        // when
        boolean result = shortLivedProvider.validateAccessToken(token);

        // then
        assertThat(result).isFalse();
    }
}
