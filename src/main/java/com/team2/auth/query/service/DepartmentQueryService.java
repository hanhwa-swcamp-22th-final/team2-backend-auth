package com.team2.auth.query.service;

import com.team2.auth.command.domain.entity.Department;
import com.team2.auth.query.mapper.DepartmentQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentQueryService {

    private final DepartmentQueryMapper departmentQueryMapper;

    public Department getDepartment(Integer id) {
        Department department = departmentQueryMapper.findById(id);
        if (department == null) {
            throw new IllegalArgumentException("부서를 찾을 수 없습니다.");
        }
        return department;
    }

    public List<Department> getAllDepartments() {
        return departmentQueryMapper.findAll();
    }

    public Department getDepartmentByName(String name) {
        Department department = departmentQueryMapper.findByDepartmentName(name);
        if (department == null) {
            throw new IllegalArgumentException("부서를 찾을 수 없습니다.");
        }
        return department;
    }
}
