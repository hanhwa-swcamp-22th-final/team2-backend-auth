package com.team2.auth.service;

import com.team2.auth.dto.TokenResponse;
import com.team2.auth.entity.RefreshToken;
import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import com.team2.auth.repository.RefreshTokenRepository;
import com.team2.auth.repository.UserRepository;
import com.team2.auth.security.JwtProvider;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private EntityManager entityManager;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .employeeNo("EMP001")
                .userName("홍길동")
                .userEmail("hong@test.com")
                .userPw(passwordEncoder.encode("rawPassword"))
                .userRole(Role.SALES)
                .userStatus(UserStatus.ACTIVE)
                .build();
        savedUser = userRepository.saveAndFlush(user);
        entityManager.clear();
    }

    // ========================================================================
    // 로그인 테스트
    // ========================================================================

    @Test
    @DisplayName("로그인 성공 시 AccessToken과 RefreshToken을 반환한다")
    void login_success() {
        // when
        TokenResponse response = authService.login("hong@test.com", "rawPassword");

        // then
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();

        // DB에 RefreshToken이 저장되었는지 확인
        assertThat(refreshTokenRepository.findByTokenValue(response.getRefreshToken())).isPresent();
    }

    @Test
    @DisplayName("로그인 성공 시 JWT에 사용자 정보가 포함된다")
    void login_success_tokenContainsUserInfo() {
        // when
        TokenResponse response = authService.login("hong@test.com", "rawPassword");

        // then
        var claims = jwtProvider.parseAccessToken(response.getAccessToken());
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(savedUser.getUserId()));
        assertThat(claims.get("email")).isEqualTo("hong@test.com");
        assertThat(claims.get("name")).isEqualTo("홍길동");
        assertThat(claims.get("role")).isEqualTo("SALES");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
    void login_userNotFound() {
        assertThatThrownBy(() -> authService.login("notexist@test.com", "password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 예외가 발생한다")
    void login_invalidPassword() {
        assertThatThrownBy(() -> authService.login("hong@test.com", "wrongPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("퇴직 상태 사용자는 로그인할 수 없다")
    void login_retiredUser() {
        User user = userRepository.findById(savedUser.getUserId()).orElseThrow();
        user.changeStatus(UserStatus.RETIRED);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThatThrownBy(() -> authService.login("hong@test.com", "rawPassword"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("로그인할 수 없는 상태");
    }

    @Test
    @DisplayName("휴직 상태 사용자는 로그인할 수 없다")
    void login_onLeaveUser() {
        User user = userRepository.findById(savedUser.getUserId()).orElseThrow();
        user.changeStatus(UserStatus.ON_LEAVE);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThatThrownBy(() -> authService.login("hong@test.com", "rawPassword"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("로그인할 수 없는 상태");
    }

    // ========================================================================
    // 토큰 갱신 테스트
    // ========================================================================

    @Test
    @DisplayName("유효한 리프레시 토큰으로 새 토큰을 발급받을 수 있다 (토큰 로테이션)")
    void refreshToken_success() {
        // given - 먼저 로그인해서 실제 토큰 발급
        TokenResponse loginResponse = authService.login("hong@test.com", "rawPassword");
        entityManager.flush();
        entityManager.clear();

        // when
        TokenResponse refreshResponse = authService.refreshToken(loginResponse.getRefreshToken());

        // then - 새 토큰 발급 확인
        assertThat(refreshResponse.getAccessToken()).isNotBlank();
        assertThat(refreshResponse.getRefreshToken()).isNotBlank();

        // 이전 토큰은 삭제되고 새 토큰이 DB에 저장됨 (로테이션)
        assertThat(refreshTokenRepository.findByTokenValue(loginResponse.getRefreshToken())).isEmpty();
        assertThat(refreshTokenRepository.findByTokenValue(refreshResponse.getRefreshToken())).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰으로 갱신 시 예외가 발생한다")
    void refreshToken_notFound() {
        assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰");
    }

    @Test
    @DisplayName("만료된 리프레시 토큰으로 갱신 시 예외가 발생하고 토큰이 삭제된다")
    void refreshToken_expired() {
        // given - 만료된 토큰을 직접 DB에 넣기
        User user = userRepository.findById(savedUser.getUserId()).orElseThrow();
        RefreshToken expiredToken = RefreshToken.builder()
                .user(user)
                .tokenValue("expired-token")
                .tokenExpiresAt(LocalDateTime.now().minusDays(1))
                .build();
        refreshTokenRepository.saveAndFlush(expiredToken);
        entityManager.clear();

        // when & then
        assertThatThrownBy(() -> authService.refreshToken("expired-token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("만료된 리프레시 토큰");

        // 만료된 토큰이 DB에서 삭제됐는지 확인
        entityManager.flush();
        entityManager.clear();
        assertThat(refreshTokenRepository.findByTokenValue("expired-token")).isEmpty();
    }

    // ========================================================================
    // 로그아웃 테스트
    // ========================================================================

    @Test
    @DisplayName("로그아웃 시 사용자의 리프레시 토큰이 모두 삭제된다")
    void logout_success() {
        // given - 로그인해서 토큰 생성
        authService.login("hong@test.com", "rawPassword");
        entityManager.flush();
        entityManager.clear();

        // when
        authService.logout(savedUser.getUserId());
        entityManager.flush();
        entityManager.clear();

        // then - 해당 사용자의 토큰이 전부 삭제됨
        User user = userRepository.findById(savedUser.getUserId()).orElseThrow();
        assertThat(refreshTokenRepository.findByUser(user)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그아웃 시 예외가 발생한다")
    void logout_userNotFound() {
        assertThatThrownBy(() -> authService.logout(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }
}
