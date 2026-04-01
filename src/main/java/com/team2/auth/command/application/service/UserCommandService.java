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

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserQueryService userQueryService;

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

    public User changeStatus(Integer id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.changeStatus(status);
        return user;
    }
}
