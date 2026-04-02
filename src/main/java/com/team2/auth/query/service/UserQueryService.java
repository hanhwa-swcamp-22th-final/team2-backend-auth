package com.team2.auth.query.service;

import com.team2.auth.command.domain.entity.User;
import com.team2.auth.command.domain.entity.enums.UserStatus;
import com.team2.auth.common.PagedResponse;
import com.team2.auth.query.dto.UserListResponse;
import com.team2.auth.query.mapper.UserQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserQueryMapper userQueryMapper;

    public User getUser(Integer id) {
        User user = userQueryMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        return user;
    }

    public List<User> getAllUsers() {
        return userQueryMapper.findAll();
    }

    public List<User> getUsersByDepartment(Integer departmentId) {
        return userQueryMapper.findByDepartmentId(departmentId);
    }

    public List<User> getUsersByStatus(UserStatus userStatus) {
        return userQueryMapper.findByUserStatus(userStatus.name());
    }

    public boolean existsByUserEmail(String email) {
        return userQueryMapper.existsByUserEmail(email) > 0;
    }

    public boolean existsByEmployeeNo(String employeeNo) {
        return userQueryMapper.existsByEmployeeNo(employeeNo) > 0;
    }

    public User getUserByEmail(String email) {
        return userQueryMapper.findByUserEmail(email);
    }

    public PagedResponse<UserListResponse> getUsers(String userName, Integer departmentId,
                                                     String userRole, String userStatus,
                                                     int page, int size) {
        int offset = page * size;
        List<UserListResponse> content = userQueryMapper.findByCondition(
                userName, departmentId, userRole, userStatus, size, offset);
        long totalElements = userQueryMapper.countByCondition(
                userName, departmentId, userRole, userStatus);
        return PagedResponse.of(content, totalElements, page, size);
    }
}
