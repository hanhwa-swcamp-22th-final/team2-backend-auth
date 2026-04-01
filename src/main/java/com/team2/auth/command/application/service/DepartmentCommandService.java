package com.team2.auth.command.application.service;

import com.team2.auth.command.domain.entity.Department;
import com.team2.auth.command.domain.repository.DepartmentRepository;
import com.team2.auth.query.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentCommandService {

    private final DepartmentRepository departmentRepository;
    private final UserQueryService userQueryService;

    public Department createDepartment(String name) {
        return departmentRepository.save(new Department(name));
    }

    public void deleteDepartment(Integer id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));

        if (!userQueryService.getUsersByDepartment(id).isEmpty()) {
            throw new IllegalStateException("소속된 사용자가 있어 삭제할 수 없습니다.");
        }

        departmentRepository.delete(department);
    }
}
