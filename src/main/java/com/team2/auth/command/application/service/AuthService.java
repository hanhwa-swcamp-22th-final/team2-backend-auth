package com.team2.auth.command.application.service;

import com.team2.auth.query.dto.TokenResponse;
import com.team2.auth.command.domain.entity.RefreshToken;
import com.team2.auth.command.domain.entity.User;
import com.team2.auth.query.mapper.RefreshTokenQueryMapper;
import com.team2.auth.query.service.UserQueryService;
import com.team2.auth.command.domain.repository.RefreshTokenRepository;
import com.team2.auth.command.domain.repository.UserRepository;
import com.team2.auth.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserQueryService userQueryService;
    private final RefreshTokenQueryMapper refreshTokenQueryMapper;

    @Transactional
    public TokenResponse login(String email, String password) {
        User user = userQueryService.getUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        if (!passwordEncoder.matches(password, user.getUserPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (!user.canLogin()) {
            throw new IllegalStateException("로그인할 수 없는 상태입니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshTokenValue = jwtProvider.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenValue(refreshTokenValue)
                .tokenExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProvider.getRefreshTokenExpiry())))
                .build();
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .user(buildUserInfo(user))
                .build();
    }

    @Transactional
    public TokenResponse refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenQueryMapper.findByTokenValue(token);
        if (refreshToken == null) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalStateException("만료된 리프레시 토큰입니다.");
        }

        // RefreshTokenQueryMapper.findByTokenValue 는 team/position 을 join 하지 않고
        // User 를 재구성한다. 그대로 JwtProvider.generateAccessToken 에 넘기면
        // team/position 이 null 이라 teamId/departmentId/positionId/positionLevel
        // 클레임이 전부 null → JJWT 가 null claim 을 드롭하면서 refresh 토큰에서
        // 구조 클레임이 사라진다(SALES 계정의 팀 스코프 필터가 0건이 되던 원인).
        // login 경로처럼 UserQueryMapper.findById 로 다시 로드해 full-join User 확보.
        User user = userQueryService.getUser(refreshToken.getUser().getUserId());
        String newAccessToken = jwtProvider.generateAccessToken(user);
        String newRefreshTokenValue = jwtProvider.generateRefreshToken();

        refreshTokenRepository.delete(refreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .tokenValue(newRefreshTokenValue)
                .tokenExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProvider.getRefreshTokenExpiry())))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .user(buildUserInfo(user))
                .build();
    }

    private TokenResponse.UserInfo buildUserInfo(User user) {
        var team = user.getTeam();
        var dept = user.getDepartment();
        var position = user.getPosition();
        return TokenResponse.UserInfo.builder()
                .userId(user.getUserId())
                .employeeNo(user.getEmployeeNo())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .userRole(user.getUserRole().name())
                .teamId(team != null ? team.getTeamId() : null)
                .teamName(team != null ? team.getTeamName() : null)
                .departmentId(dept != null ? dept.getDepartmentId() : null)
                .departmentName(dept != null ? dept.getDepartmentName() : null)
                .positionId(position != null ? position.getPositionId() : null)
                .positionLevel(position != null ? position.getPositionLevel() : null)
                .positionName(position != null ? position.getPositionName() : null)
                .build();
    }

    @Transactional
    public void logout(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        refreshTokenRepository.deleteByUser(user);
    }
}
