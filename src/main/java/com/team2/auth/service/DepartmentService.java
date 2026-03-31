package com.team2.auth.service;

import com.team2.auth.entity.Department;
import com.team2.auth.repository.DepartmentRepository;
import com.team2.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public Department createDepartment(String name) {
        return departmentRepository.save(new Department(name));
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional
    public void deleteDepartment(Integer id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));

        if (!userRepository.findByDepartmentDepartmentId(id).isEmpty()) {
            throw new IllegalStateException("소속된 사용자가 있어 삭제할 수 없습니다.");
        }

        departmentRepository.delete(department);
    }
}
