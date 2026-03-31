package com.team2.auth.service;

import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.UserStatus;
import com.team2.auth.mapper.UserQueryMapper;
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
}
