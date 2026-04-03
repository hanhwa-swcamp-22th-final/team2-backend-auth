package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.Department;
import com.team2.auth.query.service.DepartmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentQueryController {

    private final DepartmentQueryService departmentQueryService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Department>>> getAllDepartments() {
        List<EntityModel<Department>> departments = departmentQueryService.getAllDepartments().stream()
                .map(d -> EntityModel.of(d,
                        linkTo(methodOn(DepartmentQueryController.class).getAllDepartments()).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(departments,
                linkTo(methodOn(DepartmentQueryController.class).getAllDepartments()).withSelfRel()));
    }
}
