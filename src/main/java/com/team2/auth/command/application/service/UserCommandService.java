package com.team2.auth.command.application.service;

import com.team2.auth.command.application.dto.CreateUserRequest;
import com.team2.auth.command.application.dto.UpdateUserRequest;
import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.command.domain.entity.Team;
import com.team2.auth.command.domain.entity.User;
import com.team2.auth.command.domain.entity.enums.UserStatus;
import com.team2.auth.command.domain.repository.PositionRepository;
import com.team2.auth.command.domain.repository.TeamRepository;
import com.team2.auth.command.domain.repository.UserRepository;
import com.team2.auth.query.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserQueryService userQueryService;
    private final EmailService emailService;

    public User createUser(CreateUserRequest request) {
        if (userQueryService.existsByUserEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String employeeNo = generateEmployeeNo();

        User user = User.builder()
                .employeeNo(employeeNo)
                .userName(request.getName())
                .userEmail(request.getEmail())
                .userPw(passwordEncoder.encode(request.getPassword()))
                .userRole(request.getRole())
                .userStatus(UserStatus.ACTIVE)
                .build();

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
            user.assignTeam(team);
        }

        if (request.getPositionId() != null) {
            Position position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new IllegalArgumentException("직급을 찾을 수 없습니다."));
            user.assignPosition(position);
        }

        return userRepository.save(user);
    }

    private String generateEmployeeNo() {
        String prefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int nextSeq = userRepository.findMaxEmployeeNoByPrefix(prefix)
                .map(max -> Integer.parseInt(max.substring(6)) + 1)
                .orElse(1);
        if (nextSeq > 99) {
            throw new IllegalStateException("당일 사번 발급 한도(99명)를 초과했습니다.");
        }
        return prefix + String.format("%02d", nextSeq);
    }

    public User updateUser(Integer id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateInfo(request.getName(), request.getEmail());

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
            user.assignTeam(team);
        }

        if (request.getPositionId() != null) {
            Position position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new IllegalArgumentException("직급을 찾을 수 없습니다."));
            user.assignPosition(position);
        }

        return user;
    }

    public void changePassword(Integer id, String currentPw, String newPw) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPw, user.getUserPw())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.changePassword(passwordEncoder.encode(newPw));
    }

    private static final String DEFAULT_RESET_PASSWORD = "test1234";

    public void resetPassword(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.changePassword(passwordEncoder.encode(DEFAULT_RESET_PASSWORD));
    }

    public void forgotPassword(String email) {
        User user = userQueryService.getUserByEmail(email);
        // 계정 열거 공격 방어: 존재하지 않는 이메일이어도 동일한 200 OK 응답을 위해 조용히 종료한다.
        // 응답 차이로 이메일 존재 여부를 추론할 수 없게 한다.
        if (user == null) {
            log.warn("forgot-password 요청: 존재하지 않는 이메일 — silent ack 처리");
            return;
        }

        String tempPassword = generateTempPassword();
        user.changePassword(passwordEncoder.encode(tempPassword));
        try {
            emailService.sendTemporaryPassword(user.getUserEmail(), user.getUserName(), tempPassword);
        } catch (Exception e) {
            // 메일 발송 실패 시 비밀번호 변경을 롤백하기 위해 예외를 그대로 전파한다.
            // (클래스 레벨 @Transactional 이 작동)
            log.error("비밀번호 초기화 메일 발송 실패 — 트랜잭션 롤백 예정: {}", e.getMessage());
            throw new IllegalStateException("메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public User changeStatus(Integer id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.changeStatus(status);
        return user;
    }
}
