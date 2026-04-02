package com.team2.auth.command.application.service;

import com.team2.auth.command.application.dto.CreateUserRequest;
import com.team2.auth.command.application.dto.UpdateUserRequest;
import com.team2.auth.command.domain.entity.Department;
import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.command.domain.entity.User;
import com.team2.auth.command.domain.entity.enums.UserStatus;
import com.team2.auth.command.domain.repository.DepartmentRepository;
import com.team2.auth.command.domain.repository.PositionRepository;
import com.team2.auth.command.domain.repository.UserRepository;
import com.team2.auth.query.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserQueryService userQueryService;
    private final EmailService emailService;

    public User createUser(CreateUserRequest request) {
        if (userQueryService.existsByUserEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userQueryService.existsByEmployeeNo(request.getEmployeeNo())) {
            throw new IllegalArgumentException("이미 사용 중인 사번입니다.");
        }

        User user = User.builder()
                .employeeNo(request.getEmployeeNo())
                .userName(request.getName())
                .userEmail(request.getEmail())
                .userPw(passwordEncoder.encode(request.getPassword()))
                .userRole(request.getRole())
                .userStatus(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    public User updateUser(Integer id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateInfo(request.getName(), request.getEmail());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));
            user.assignDepartment(department);
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
        if (user == null) {
            throw new IllegalArgumentException("등록되지 않은 이메일입니다.");
        }

        String tempPassword = generateTempPassword();
        user.changePassword(passwordEncoder.encode(tempPassword));
        emailService.sendTemporaryPassword(user.getUserEmail(), user.getUserName(), tempPassword);
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
