package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.CreateDepartmentRequest;
import com.team2.auth.command.application.dto.UpdateDepartmentRequest;
import com.team2.auth.command.domain.entity.Department;
import com.team2.auth.command.application.service.DepartmentCommandService;
import com.team2.auth.query.controller.DepartmentQueryController;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentCommandController {

    private final DepartmentCommandService departmentCommandService;

    @PostMapping
    public ResponseEntity<EntityModel<Department>> createDepartment(@RequestBody CreateDepartmentRequest request) {
        Department department = departmentCommandService.createDepartment(request.getName());
        EntityModel<Department> model = EntityModel.of(department,
                linkTo(methodOn(DepartmentQueryController.class).getAllDepartments()).withRel("departments"));
        URI location = linkTo(methodOn(DepartmentQueryController.class).getAllDepartments()).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Department>> updateDepartment(@PathVariable Integer id,
                                                       @RequestBody UpdateDepartmentRequest request) {
        Department dept = departmentCommandService.updateDepartment(id, request.getName());
        return ResponseEntity.ok(EntityModel.of(dept,
                linkTo(methodOn(DepartmentQueryController.class).getAllDepartments()).withRel("departments")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Integer id) {
        departmentCommandService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
