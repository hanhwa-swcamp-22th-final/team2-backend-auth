package com.team2.auth.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.command.domain.entity.User;
import com.team2.auth.command.domain.entity.enums.Role;
import com.team2.auth.command.domain.entity.enums.UserStatus;
import com.team2.auth.command.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@Transactional
class JwtProviderTest {

    private static final long ACCESS_TOKEN_EXPIRY = 3600000L;
    private static final long REFRESH_TOKEN_EXPIRY = 604800000L;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    private static KeyPair generateTestKeyPair() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            return gen.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .employeeNo("EMP001")
                .userName("홍길동")
                .userEmail("hong@test.com")
                .userPw("encodedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.ACTIVE)
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("AccessToken 생성 시 null이 아니고 빈 문자열이 아니다")
    void generateAccessToken_returnsNonEmptyToken() {
        String token = jwtProvider.generateAccessToken(testUser);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("AccessToken 파싱 시 subject와 claim이 정확하다")
    void parseAccessToken_returnsCorrectClaims() {
        String token = jwtProvider.generateAccessToken(testUser);
        Claims claims = jwtProvider.parseAccessToken(token);
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(testUser.getUserId()));
        assertThat(claims.get("email", String.class)).isEqualTo("hong@test.com");
        assertThat(claims.get("name", String.class)).isEqualTo("홍길동");
        assertThat(claims.get("role", String.class)).isEqualTo("SALES");
    }

    @Test
    @DisplayName("RefreshToken 생성 시 UUID 형식이다")
    void generateRefreshToken_returnsUuidFormat() {
        String refreshToken = jwtProvider.generateRefreshToken();
        assertThat(refreshToken).matches(
                "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
        );
    }

    @Test
    @DisplayName("유효한 토큰 검증 시 true를 반환한다")
    void validateAccessToken_withValidToken_returnsTrue() {
        String token = jwtProvider.generateAccessToken(testUser);
        assertThat(jwtProvider.validateAccessToken(token)).isTrue();
    }

    @Test
    @DisplayName("변조된 토큰 검증 시 false를 반환한다")
    void validateAccessToken_withTamperedToken_returnsFalse() {
        String token = jwtProvider.generateAccessToken(testUser);
        assertThat(jwtProvider.validateAccessToken(token + "tampered")).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 시 false를 반환한다")
    void validateAccessToken_withExpiredToken_returnsFalse() throws InterruptedException {
        KeyPair keyPair = generateTestKeyPair();
        JwtProvider shortLivedProvider = new JwtProvider(
                (RSAPrivateKey) keyPair.getPrivate(),
                (RSAPublicKey) keyPair.getPublic(),
                1L, REFRESH_TOKEN_EXPIRY, "test");
        String token = shortLivedProvider.generateAccessToken(testUser);
        Thread.sleep(50);
        assertThat(shortLivedProvider.validateAccessToken(token)).isFalse();
    }

    @Test
    @DisplayName("RefreshToken 만료 시간을 반환한다")
    void getRefreshTokenExpiry_returnsConfiguredValue() {
        assertThat(jwtProvider.getRefreshTokenExpiry()).isEqualTo(REFRESH_TOKEN_EXPIRY);
    }

    @Test
    @DisplayName("AccessToken은 3개의 dot으로 구분된 JWT 구조이다")
    void generateAccessToken_hasThreePartStructure() {
        String token = jwtProvider.generateAccessToken(testUser);
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
        assertThat(parts[0]).isNotBlank();
        assertThat(parts[1]).isNotBlank();
        assertThat(parts[2]).isNotBlank();
    }

    @Test
    @DisplayName("JWT header에 alg 필드가 RS256이다")
    void generateAccessToken_headerContainsRS256Algorithm() throws Exception {
        String token = jwtProvider.generateAccessToken(testUser);
        String headerJson = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode header = mapper.readTree(headerJson);
        assertThat(header.has("alg")).isTrue();
        assertThat(header.get("alg").asText()).isEqualTo("RS256");
    }

    @Test
    @DisplayName("JWT header에 kid 필드가 존재한다")
    void generateAccessToken_headerContainsKid() throws Exception {
        String token = jwtProvider.generateAccessToken(testUser);
        String headerJson = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]));
        JsonNode header = new ObjectMapper().readTree(headerJson);
        assertThat(header.has("kid")).isTrue();
        assertThat(header.get("kid").asText()).isNotBlank();
    }

    @Test
    @DisplayName("JWT payload에 sub, email, name, role 클레임이 올바른 값으로 존재한다")
    void generateAccessToken_payloadContainsCorrectClaims() throws Exception {
        String token = jwtProvider.generateAccessToken(testUser);
        String payloadJson = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        JsonNode payload = new ObjectMapper().readTree(payloadJson);
        assertThat(payload.get("sub").asText()).isEqualTo(String.valueOf(testUser.getUserId()));
        assertThat(payload.get("email").asText()).isEqualTo("hong@test.com");
        assertThat(payload.get("name").asText()).isEqualTo("홍길동");
        assertThat(payload.get("role").asText()).isEqualTo("SALES");
        assertThat(payload.has("iat")).isTrue();
        assertThat(payload.has("exp")).isTrue();
    }

    @Test
    @DisplayName("다른 RSA 키로 서명된 토큰은 검증에 실패한다")
    void validateAccessToken_withDifferentKey_returnsFalse() {
        KeyPair otherKeyPair = generateTestKeyPair();
        JwtProvider otherProvider = new JwtProvider(
                (RSAPrivateKey) otherKeyPair.getPrivate(),
                (RSAPublicKey) otherKeyPair.getPublic(),
                ACCESS_TOKEN_EXPIRY, REFRESH_TOKEN_EXPIRY, "test");
        String tokenFromOther = otherProvider.generateAccessToken(testUser);
        assertThat(jwtProvider.validateAccessToken(tokenFromOther)).isFalse();
    }

    @Test
    @DisplayName("null 토큰 검증 시 false를 반환한다")
    void validateAccessToken_withNull_returnsFalse() {
        assertThat(jwtProvider.validateAccessToken(null)).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 토큰 검증 시 false를 반환한다")
    void validateAccessToken_withEmptyString_returnsFalse() {
        assertThat(jwtProvider.validateAccessToken("")).isFalse();
    }
}
