package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.Department;
import com.team2.auth.query.service.DepartmentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "부서 조회", description = "부서 목록 조회 API")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentQueryController {

    private final DepartmentQueryService departmentQueryService;

    @Operation(summary = "전체 부서 목록 조회", description = "등록된 모든 부서 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
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
