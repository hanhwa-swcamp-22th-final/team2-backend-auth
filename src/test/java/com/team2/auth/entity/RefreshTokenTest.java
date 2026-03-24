package com.team2.auth.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenTest {

    @Test
    @DisplayName("리프레시 토큰 생성 성공: 토큰값과 만료시각이 설정된다.")
    void createRefreshToken_Success() {
        // given
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        // when
        RefreshToken token = RefreshToken.builder()
                .token("sample-refresh-token-value")
                .expiresAt(expiresAt)
                .build();

        // then
        assertEquals("sample-refresh-token-value", token.getToken());
        assertEquals(expiresAt, token.getExpiresAt());
    }

    @Test
    @DisplayName("토큰 만료 확인: 만료 시각이 지나면 만료 상태이다.")
    void isExpired_PastExpiry_ReturnsTrue() {
        // given
        RefreshToken token = RefreshToken.builder()
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        // when & then
        assertTrue(token.isExpired());
    }

    @Test
    @DisplayName("토큰 만료 확인: 만료 시각 전이면 유효 상태이다.")
    void isExpired_FutureExpiry_ReturnsFalse() {
        // given
        RefreshToken token = RefreshToken.builder()
                .token("valid-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        // when & then
        assertFalse(token.isExpired());
    }
}
