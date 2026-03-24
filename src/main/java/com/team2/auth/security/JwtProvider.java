package com.team2.auth.security;

import com.team2.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    public String generateAccessToken(User user) {
        // JWT 생성 로직 (추후 구현)
        return "access-token";
    }

    public String generateRefreshToken() {
        // RefreshToken 생성 로직 (추후 구현)
        return "refresh-token";
    }

    public long getRefreshTokenExpiry() {
        return 7 * 24 * 60 * 60 * 1000L; // 7일
    }
}
