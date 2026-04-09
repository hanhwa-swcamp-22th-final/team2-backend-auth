package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.CreateDepartmentRequest;
import com.team2.auth.command.application.dto.UpdateDepartmentRequest;
import com.team2.auth.command.domain.entity.Department;
import com.team2.auth.command.application.service.DepartmentCommandService;
import com.team2.auth.query.controller.DepartmentQueryController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "부서 관리 (Command)", description = "부서 생성, 수정, 삭제 API")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentCommandController {

    private final DepartmentCommandService departmentCommandService;

    @Operation(summary = "부서 생성", description = "새로운 부서를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "부서 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (중복된 부서명 등)")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Department>> createDepartment(@RequestBody CreateDepartmentRequest request) {
        Department department = departmentCommandService.createDepartment(request.getName());
        EntityModel<Department> model = EntityModel.of(department,
                linkTo(methodOn(DepartmentQueryController.class).getAllDepartments()).withRel("departments"));
        URI location = linkTo(methodOn(DepartmentQueryController.class).getAllDepartments()).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @Operation(summary = "부서 수정", description = "기존 부서의 이름을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "부서 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "부서를 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Department>> updateDepartment(
            @Parameter(description = "부서 ID") @PathVariable("id") Integer id,
            @RequestBody UpdateDepartmentRequest request) {
        Department dept = departmentCommandService.updateDepartment(id, request.getName());
        return ResponseEntity.ok(EntityModel.of(dept,
                linkTo(methodOn(DepartmentQueryController.class).getAllDepartments()).withRel("departments")));
    }

    @Operation(summary = "부서 삭제", description = "부서를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "부서 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "부서를 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "부서 ID") @PathVariable("id") Integer id) {
        departmentCommandService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
