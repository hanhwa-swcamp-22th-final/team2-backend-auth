package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.ChangePasswordRequest;
import com.team2.auth.command.application.dto.ChangeStatusRequest;
import com.team2.auth.command.application.dto.CreateUserRequest;
import com.team2.auth.command.application.dto.UpdateUserRequest;
import com.team2.auth.command.domain.entity.User;
import com.team2.auth.command.application.service.UserCommandService;
import com.team2.auth.query.controller.UserQueryController;
import com.team2.auth.query.dto.UserDetailResponse;
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

@Tag(name = "사용자 관리 (Command)", description = "사용자 생성, 수정, 비밀번호 변경, 상태 변경 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserCommandController {

    private final UserCommandService userCommandService;

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "사용자 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (중복 이메일, 필수 항목 누락 등)")
    })
    @PostMapping
    public ResponseEntity<EntityModel<UserDetailResponse>> createUser(@RequestBody CreateUserRequest request) {
        User user = userCommandService.createUser(request);
        UserDetailResponse response = UserDetailResponse.from(user);
        EntityModel<UserDetailResponse> model = EntityModel.of(response,
                linkTo(methodOn(UserQueryController.class).getUser(user.getUserId())).withSelfRel(),
                linkTo(methodOn(UserQueryController.class).getUsers(null, null, null, null, null, 0, 10)).withRel("users"));
        URI location = linkTo(methodOn(UserQueryController.class).getUser(user.getUserId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자의 이름, 이메일, 부서, 직급 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserDetailResponse>> updateUser(
            @Parameter(description = "사용자 ID") @PathVariable("id") Integer id,
            @RequestBody UpdateUserRequest request) {
        User user = userCommandService.updateUser(id, request);
        return ResponseEntity.ok(EntityModel.of(UserDetailResponse.from(user),
                linkTo(methodOn(UserQueryController.class).getUser(id)).withSelfRel(),
                linkTo(methodOn(UserQueryController.class).getUsers(null, null, null, null, null, 0, 10)).withRel("users")));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인한 후 새 비밀번호로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치 또는 잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "사용자 ID") @PathVariable("id") Integer id,
            @RequestBody ChangePasswordRequest request) {
        userCommandService.changePassword(id, request.getCurrentPw(), request.getNewPw());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 초기화", description = "관리자가 사용자의 비밀번호를 초기화하여 이메일로 발송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 초기화 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/{id}/password/reset")
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "사용자 ID") @PathVariable("id") Integer id) {
        userCommandService.resetPassword(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 상태 변경", description = "사용자의 활성/비활성 상태를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<EntityModel<UserDetailResponse>> changeStatus(
            @Parameter(description = "사용자 ID") @PathVariable("id") Integer id,
            @RequestBody ChangeStatusRequest request) {
        User user = userCommandService.changeStatus(id, request.getStatus());
        return ResponseEntity.ok(EntityModel.of(UserDetailResponse.from(user),
                linkTo(methodOn(UserQueryController.class).getUser(id)).withSelfRel()));
    }
}
