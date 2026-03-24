package com.team2.auth.repository;

import com.team2.auth.entity.RefreshToken;
import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = MybatisAutoConfiguration.class)
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .employeeNo("EMP001")
                .name("홍길동")
                .email("hong@test.com")
                .pw("encodedPassword")
                .role(Role.SALES)
                .status(UserStatus.재직)
                .build();
        savedUser = userRepository.save(user);

        RefreshToken token = RefreshToken.builder()
                .user(savedUser)
                .token("refresh-token-value-123")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(token);
    }

    @Test
    @DisplayName("토큰 값으로 리프레시 토큰을 조회할 수 있다")
    void findByToken() {
        // given
        String tokenValue = "refresh-token-value-123";

        // when
        Optional<RefreshToken> result = refreshTokenRepository.findByToken(tokenValue);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("존재하지 않는 토큰으로 조회하면 빈 Optional을 반환한다")
    void findByToken_notFound() {
        // given & when
        Optional<RefreshToken> result = refreshTokenRepository.findByToken("invalid-token");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자로 리프레시 토큰을 조회할 수 있다")
    void findByUser() {
        // given & when
        Optional<RefreshToken> result = refreshTokenRepository.findByUser(savedUser);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("refresh-token-value-123");
    }

    @Test
    @DisplayName("사용자의 리프레시 토큰을 삭제할 수 있다")
    void deleteByUser() {
        // given
        assertThat(refreshTokenRepository.findByUser(savedUser)).isPresent();

        // when
        refreshTokenRepository.deleteByUser(savedUser);

        // then
        assertThat(refreshTokenRepository.findByUser(savedUser)).isEmpty();
    }
}
