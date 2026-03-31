package com.team2.auth.service;

import com.team2.auth.entity.Department;
import com.team2.auth.mapper.UserQueryMapper;
import com.team2.auth.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentCommandService {

    private final DepartmentRepository departmentRepository;
    private final UserQueryMapper userQueryMapper;

    public Department createDepartment(String name) {
        return departmentRepository.save(new Department(name));
    }

    public void deleteDepartment(Integer id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));

        if (!userQueryMapper.findByDepartmentId(id).isEmpty()) {
            throw new IllegalStateException("소속된 사용자가 있어 삭제할 수 없습니다.");
        }

        departmentRepository.delete(department);
    }
}
