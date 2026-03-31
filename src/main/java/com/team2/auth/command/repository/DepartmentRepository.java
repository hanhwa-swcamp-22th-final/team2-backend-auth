package com.team2.auth.command.repository;

import com.team2.auth.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    Optional<Department> findByDepartmentName(String departmentName);
}
