package com.team2.auth.service;

import com.team2.auth.dto.CreateUserRequest;
import com.team2.auth.dto.UpdateUserRequest;
import com.team2.auth.entity.Department;
import com.team2.auth.entity.Position;
import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.UserStatus;
import com.team2.auth.mapper.UserQueryMapper;
import com.team2.auth.repository.DepartmentRepository;
import com.team2.auth.repository.PositionRepository;
import com.team2.auth.repository.UserRepository;
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
    private final UserQueryMapper userQueryMapper;

    public User createUser(CreateUserRequest request) {
        if (userQueryMapper.existsByUserEmail(request.getEmail()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userQueryMapper.existsByEmployeeNo(request.getEmployeeNo()) > 0) {
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
