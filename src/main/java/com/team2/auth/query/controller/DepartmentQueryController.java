package com.team2.auth.query.controller;

import com.team2.auth.entity.Department;
import com.team2.auth.query.service.DepartmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentQueryController {

    private final DepartmentQueryService departmentQueryService;

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentQueryService.getAllDepartments());
    }
}
