package com.team2.auth.service;

import com.team2.auth.dto.CreateUserRequest;
import com.team2.auth.dto.UpdateUserRequest;
import com.team2.auth.entity.Department;
import com.team2.auth.entity.Position;
import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.UserStatus;
import com.team2.auth.repository.DepartmentRepository;
import com.team2.auth.repository.PositionRepository;
import com.team2.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByEmployeeNo(request.getEmployeeNo())) {
            throw new IllegalArgumentException("이미 사용 중인 사번입니다.");
        }

        User user = User.builder()
                .employeeNo(request.getEmployeeNo())
                .name(request.getName())
                .email(request.getEmail())
                .pw(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.재직)
                .build();

        return userRepository.save(user);
    }

    public User getUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public User updateUser(Integer id, UpdateUserRequest request) {
        User user = getUser(id);
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

    @Transactional
    public User changeStatus(Integer id, UserStatus status) {
        User user = getUser(id);
        user.changeStatus(status);
        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
