package com.team2.auth.command.repository;

import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUserEmail(String userEmail);

    Optional<User> findByEmployeeNo(String employeeNo);

    boolean existsByUserEmail(String userEmail);

    boolean existsByEmployeeNo(String employeeNo);

    List<User> findByDepartmentDepartmentId(Integer departmentId);

    List<User> findByUserStatus(UserStatus userStatus);
}
