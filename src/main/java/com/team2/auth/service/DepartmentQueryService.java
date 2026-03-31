package com.team2.auth.service;

import com.team2.auth.entity.Department;
import com.team2.auth.mapper.DepartmentQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentQueryService {

    private final DepartmentQueryMapper departmentQueryMapper;

    public List<Department> getAllDepartments() {
        return departmentQueryMapper.findAll();
    }
}
