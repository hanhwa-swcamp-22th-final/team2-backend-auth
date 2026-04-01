package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.CreateDepartmentRequest;
import com.team2.auth.command.domain.entity.Department;
import com.team2.auth.command.application.service.DepartmentCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentCommandController {

    private final DepartmentCommandService departmentCommandService;

    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody CreateDepartmentRequest request) {
        Department department = departmentCommandService.createDepartment(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(department);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Integer id) {
        departmentCommandService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
