package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.User;
import com.team2.auth.common.PagedResponse;
import com.team2.auth.query.dto.UserDetailResponse;
import com.team2.auth.query.dto.UserListResponse;
import com.team2.auth.query.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "사용자 조회", description = "사용자 목록 및 상세 조회 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserQueryService userQueryService;

    @Operation(summary = "사용자 목록 조회", description = "필터 조건과 페이징을 적용하여 사용자 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<UserListResponse>>> getUsers(
            @Parameter(description = "사용자 이름 (부분 검색)") @RequestParam(name = "userName", required = false) String userName,
            @Parameter(description = "팀 ID") @RequestParam(name = "teamId", required = false) Integer teamId,
            @Parameter(description = "부서 ID (팀 경유 필터)") @RequestParam(name = "departmentId", required = false) Integer departmentId,
            @Parameter(description = "사용자 역할 (ADMIN, USER 등)") @RequestParam(name = "userRole", required = false) String userRole,
            @Parameter(description = "사용자 상태 (ACTIVE, INACTIVE 등)") @RequestParam(name = "userStatus", required = false) String userStatus,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "10") int size) {
        PagedResponse<UserListResponse> result = userQueryService.getUsers(
                userName, teamId, departmentId, userRole, userStatus, page, size);
        List<UserListResponse> content = result != null && result.content() != null
                ? result.content() : List.of();
        List<EntityModel<UserListResponse>> models = content.stream()
                .filter(java.util.Objects::nonNull)
                .map(EntityModel::of).toList();
        long total = result != null ? result.totalElements() : 0;
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(size, page, total);
        return ResponseEntity.ok(PagedModel.of(models, metadata));
    }

    @Operation(summary = "뷰어 선택용 사용자 목록", description = "인증된 사용자가 기록패키지 뷰어 등에 지정할 수 있는 active 사용자 목록. 최소 정보만 반환.")
    @GetMapping("/viewable")
    public ResponseEntity<List<UserListResponse>> getViewableUsers(
            @Parameter(description = "팀 ID (선택)") @RequestParam(name = "teamId", required = false) Integer teamId,
            @Parameter(description = "부서 ID (선택)") @RequestParam(name = "departmentId", required = false) Integer departmentId) {
        PagedResponse<UserListResponse> result = userQueryService.getUsers(
                null, teamId, departmentId, null, "active", 0, 1000);
        List<UserListResponse> content = result != null && result.content() != null
                ? result.content() : List.of();
        return ResponseEntity.ok(content);
    }

    @Operation(summary = "사용자 상세 조회", description = "사용자 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserDetailResponse>> getUser(
            @Parameter(description = "사용자 ID") @PathVariable("id") Integer id) {
        User user = userQueryService.getUser(id);
        UserDetailResponse response = UserDetailResponse.from(user);
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(UserQueryController.class).getUser(id)).withSelfRel(),
                linkTo(methodOn(UserQueryController.class).getUsers(null, null, null, null, null, 0, 10)).withRel("users")));
    }

    // 내부 전용: Documents → Auth 등 서비스 간 호출용. X-Internal-Token 으로 보호된다.
    // 사용자 세션 JWT 없이 동작해야 하므로 일반 /{id} (hasRole("ADMIN")) 와 분리.
    @Operation(summary = "내부 사용자 조회", description = "MSA 내부 호출 전용 (X-Internal-Token 필수). 게이트웨이에서 외부 denyAll.")
    @GetMapping("/internal/{id}")
    public UserDetailResponse getUserInternal(
            @Parameter(description = "사용자 ID") @PathVariable("id") Integer id) {
        User user = userQueryService.getUser(id);
        return UserDetailResponse.from(user);
    }
}
