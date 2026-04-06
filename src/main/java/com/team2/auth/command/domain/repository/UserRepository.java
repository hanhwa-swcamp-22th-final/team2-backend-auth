package com.team2.auth.command.domain.repository;

import com.team2.auth.command.domain.entity.User;
import com.team2.auth.command.domain.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUserEmail(String userEmail);

    Optional<User> findByEmployeeNo(String employeeNo);

    boolean existsByUserEmail(String userEmail);

    boolean existsByEmployeeNo(String employeeNo);

    List<User> findByDepartmentDepartmentId(Integer departmentId);

    List<User> findByUserStatus(UserStatus userStatus);

    @Query("SELECT MAX(u.employeeNo) FROM User u WHERE u.employeeNo LIKE :prefix%")
    Optional<String> findMaxEmployeeNoByPrefix(@Param("prefix") String prefix);
}
