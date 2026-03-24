package com.team2.auth.service;

import com.team2.auth.dto.TokenResponse;
import com.team2.auth.entity.RefreshToken;
import com.team2.auth.entity.User;
import com.team2.auth.repository.RefreshTokenRepository;
import com.team2.auth.repository.UserRepository;
import com.team2.auth.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (!user.canLogin()) {
            throw new IllegalStateException("로그인할 수 없는 상태입니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshTokenValue = jwtProvider.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProvider.getRefreshTokenExpiry())))
                .build();
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .build();
    }

    @Transactional
    public TokenResponse refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalStateException("만료된 리프레시 토큰입니다.");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtProvider.generateAccessToken(user);
        String newRefreshTokenValue = jwtProvider.generateRefreshToken();

        refreshTokenRepository.delete(refreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(newRefreshTokenValue)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProvider.getRefreshTokenExpiry())))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .build();
    }

    @Transactional
    public void logout(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        refreshTokenRepository.deleteByUser(user);
    }
}
