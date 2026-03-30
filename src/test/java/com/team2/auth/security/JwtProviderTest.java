package com.team2.auth.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Base64;

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

    @Test
    @DisplayName("RefreshToken 만료 시간을 반환한다")
    void getRefreshTokenExpiry_returnsConfiguredValue() {
        // when
        long expiry = jwtProvider.getRefreshTokenExpiry();

        // then
        assertThat(expiry).isEqualTo(REFRESH_TOKEN_EXPIRY);
    }

    // ========================================================================
    // JWT 구조 및 JSON Payload 검증
    // ========================================================================

    @Test
    @DisplayName("AccessToken은 3개의 dot으로 구분된 JWT 구조이다")
    void generateAccessToken_hasThreePartStructure() {
        // when
        String token = jwtProvider.generateAccessToken(testUser);

        // then
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
        assertThat(parts[0]).isNotBlank(); // header
        assertThat(parts[1]).isNotBlank(); // payload
        assertThat(parts[2]).isNotBlank(); // signature
    }

    @Test
    @DisplayName("JWT header에 alg 필드가 존재하고 HMAC-SHA 계열 알고리즘이다")
    void generateAccessToken_headerContainsHmacShaAlgorithm() throws Exception {
        // given
        String token = jwtProvider.generateAccessToken(testUser);
        String headerJson = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]));
        ObjectMapper mapper = new ObjectMapper();

        // when
        JsonNode header = mapper.readTree(headerJson);

        // then
        assertThat(header.has("alg")).isTrue();
        assertThat(header.get("alg").asText()).startsWith("HS");
    }

    @Test
    @DisplayName("JWT payload에 sub, email, name, role 클레임이 올바른 값으로 존재한다")
    void generateAccessToken_payloadContainsCorrectClaims() throws Exception {
        // given
        String token = jwtProvider.generateAccessToken(testUser);
        String payloadJson = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        ObjectMapper mapper = new ObjectMapper();

        // when
        JsonNode payload = mapper.readTree(payloadJson);

        // then
        assertThat(payload.get("sub").asText()).isEqualTo("1");
        assertThat(payload.get("email").asText()).isEqualTo("hong@test.com");
        assertThat(payload.get("name").asText()).isEqualTo("홍길동");
        assertThat(payload.get("role").asText()).isEqualTo("SALES");
        assertThat(payload.has("iat")).isTrue();
        assertThat(payload.has("exp")).isTrue();
    }

    @Test
    @DisplayName("다른 시크릿 키로 서명된 토큰은 검증에 실패한다")
    void validateAccessToken_withDifferentSecret_returnsFalse() {
        // given
        JwtProvider otherProvider = new JwtProvider(
                "completelyDifferentSecretKeyThatIsLongEnoughForHS256!!", ACCESS_TOKEN_EXPIRY, REFRESH_TOKEN_EXPIRY);
        String tokenFromOther = otherProvider.generateAccessToken(testUser);

        // when
        boolean result = jwtProvider.validateAccessToken(tokenFromOther);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("null 토큰 검증 시 false를 반환한다")
    void validateAccessToken_withNull_returnsFalse() {
        // when
        boolean result = jwtProvider.validateAccessToken(null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 토큰 검증 시 false를 반환한다")
    void validateAccessToken_withEmptyString_returnsFalse() {
        // when
        boolean result = jwtProvider.validateAccessToken("");

        // then
        assertThat(result).isFalse();
    }
}
