package com.team2.auth.repository;

import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmployeeNo(String employeeNo);

    boolean existsByEmail(String email);

    boolean existsByEmployeeNo(String employeeNo);

    List<User> findByDepartmentId(Integer departmentId);

    List<User> findByStatus(UserStatus status);
}
