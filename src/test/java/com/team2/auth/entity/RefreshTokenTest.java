package com.team2.auth.entity;

import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import com.team2.auth.repository.RefreshTokenRepository;
import com.team2.auth.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
class RefreshTokenTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User createAndSaveUser(String employeeNo, String email) {
        User user = User.builder()
                .employeeNo(employeeNo)
                .name("테스트유저")
                .email(email)
                .pw("hashedPassword")
                .role(Role.SALES)
                .status(UserStatus.재직)
                .build();
        return userRepository.save(user);
    }

    @Test
    @DisplayName("리프레시 토큰 생성 성공: 토큰값과 만료시각이 설정된다.")
    void createRefreshToken_Success() {
        // given
        User user = createAndSaveUser("EMP001", "user1@test.com");
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        // when
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token("sample-refresh-token-value")
                .expiresAt(expiresAt)
                .build();

        // then - 도메인 로직 검증
        assertEquals("sample-refresh-token-value", token.getToken());
        assertEquals(expiresAt, token.getExpiresAt());

        // DB 저장 후 재조회 검증
        refreshTokenRepository.save(token);
        entityManager.flush();
        entityManager.clear();

        RefreshToken found = refreshTokenRepository.findById(token.getId()).orElseThrow();
        assertEquals("sample-refresh-token-value", found.getToken());
        assertNotNull(found.getExpiresAt());
        assertNotNull(found.getCreatedAt());
    }

    @Test
    @DisplayName("토큰 만료 확인: 만료 시각이 지나면 만료 상태이다.")
    void isExpired_PastExpiry_ReturnsTrue() {
        // given
        User user = createAndSaveUser("EMP002", "user2@test.com");
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        // when & then
        assertTrue(token.isExpired());

        // DB 저장 후 재조회 검증
        refreshTokenRepository.save(token);
        entityManager.flush();
        entityManager.clear();

        RefreshToken found = refreshTokenRepository.findById(token.getId()).orElseThrow();
        assertTrue(found.isExpired());
    }

    @Test
    @DisplayName("토큰 만료 확인: 만료 시각 전이면 유효 상태이다.")
    void isExpired_FutureExpiry_ReturnsFalse() {
        // given
        User user = createAndSaveUser("EMP003", "user3@test.com");
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token("valid-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        // when & then
        assertFalse(token.isExpired());

        // DB 저장 후 재조회 검증
        refreshTokenRepository.save(token);
        entityManager.flush();
        entityManager.clear();

        RefreshToken found = refreshTokenRepository.findById(token.getId()).orElseThrow();
        assertFalse(found.isExpired());
    }
}
