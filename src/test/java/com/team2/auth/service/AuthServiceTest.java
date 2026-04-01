package com.team2.auth.service;

import com.team2.auth.dto.TokenResponse;
import com.team2.auth.entity.RefreshToken;
import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import com.team2.auth.query.mapper.RefreshTokenQueryMapper;
import com.team2.auth.query.service.UserQueryService;
import com.team2.auth.command.repository.RefreshTokenRepository;
import com.team2.auth.command.repository.UserRepository;
import com.team2.auth.command.service.AuthService;
import com.team2.auth.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private RefreshTokenQueryMapper refreshTokenQueryMapper;

    @InjectMocks
    private AuthService authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .employeeNo("EMP001")
                .userName("홍길동")
                .userEmail("hong@test.com")
                .userPw("encodedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("로그인 성공 시 AccessToken과 RefreshToken을 반환한다")
    void login_success() {
        // given
        given(userQueryService.getUserByEmail("hong@test.com")).willReturn(activeUser);
        given(passwordEncoder.matches("rawPassword", "encodedPassword")).willReturn(true);
        given(jwtProvider.generateAccessToken(activeUser)).willReturn("access-token-value");
        given(jwtProvider.generateRefreshToken()).willReturn("refresh-token-value");
        given(jwtProvider.getRefreshTokenExpiry()).willReturn(604800000L);
        given(refreshTokenRepository.save(any(RefreshToken.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        TokenResponse response = authService.login("hong@test.com", "rawPassword");

        // then
        assertThat(response.getAccessToken()).isEqualTo("access-token-value");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-value");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
    void login_userNotFound() {
        // given
        given(userQueryService.getUserByEmail("notexist@test.com")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.login("notexist@test.com", "password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 예외가 발생한다")
    void login_invalidPassword() {
        // given
        given(userQueryService.getUserByEmail("hong@test.com")).willReturn(activeUser);
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login("hong@test.com", "wrongPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("재직 상태가 아닌 사용자는 로그인할 수 없다")
    void login_inactiveUser() {
        // given
        User retiredUser = User.builder()
                .employeeNo("EMP002")
                .userName("김철수")
                .userEmail("kim@test.com")
                .userPw("encodedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.RETIRED)
                .build();
        given(userQueryService.getUserByEmail("kim@test.com")).willReturn(retiredUser);
        given(passwordEncoder.matches("password", "encodedPassword")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.login("kim@test.com", "password"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("로그인할 수 없는 상태");
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 새 토큰을 발급받을 수 있다")
    void refreshToken_success() {
        // given
        RefreshToken refreshToken = RefreshToken.builder()
                .user(activeUser)
                .tokenValue("valid-refresh-token")
                .tokenExpiresAt(LocalDateTime.now().plusDays(7))
                .build();
        given(refreshTokenQueryMapper.findByTokenValue("valid-refresh-token")).willReturn(refreshToken);
        given(jwtProvider.generateAccessToken(activeUser)).willReturn("new-access-token");
        given(jwtProvider.generateRefreshToken()).willReturn("new-refresh-token");
        given(jwtProvider.getRefreshTokenExpiry()).willReturn(604800000L);
        given(refreshTokenRepository.save(any(RefreshToken.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        TokenResponse response = authService.refreshToken("valid-refresh-token");

        // then
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰으로 갱신 시 예외가 발생한다")
    void refreshToken_notFound() {
        // given
        given(refreshTokenQueryMapper.findByTokenValue("invalid-token")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰");
    }

    @Test
    @DisplayName("만료된 리프레시 토큰으로 갱신 시 예외가 발생한다")
    void refreshToken_expired() {
        // given
        RefreshToken expiredToken = RefreshToken.builder()
                .user(activeUser)
                .tokenValue("expired-token")
                .tokenExpiresAt(LocalDateTime.now().minusDays(1))
                .build();
        given(refreshTokenQueryMapper.findByTokenValue("expired-token")).willReturn(expiredToken);

        // when & then
        assertThatThrownBy(() -> authService.refreshToken("expired-token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("만료된 리프레시 토큰");
    }

    @Test
    @DisplayName("로그아웃 시 사용자의 리프레시 토큰이 삭제된다")
    void logout_success() {
        // given
        given(userRepository.findById(1)).willReturn(Optional.of(activeUser));

        // when
        authService.logout(1);

        // then
        verify(refreshTokenRepository).deleteByUser(activeUser);
    }
}
