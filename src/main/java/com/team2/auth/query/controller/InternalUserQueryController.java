package com.team2.auth.query.controller;

import com.team2.auth.common.PagedResponse;
import com.team2.auth.query.dto.UserListResponse;
import com.team2.auth.query.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 서비스 간 내부 호출 전용 사용자 조회 API.
 * 인증: X-Internal-Token 헤더 (InternalApiTokenFilter 가 검증).
 * Gateway 에서 /api/**\/internal/** 경로를 denyAll 로 외부 차단한다.
 */
@Tag(name = "사용자 내부 조회", description = "서비스 간 시스템 호출 전용. X-Internal-Token 필요")
@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class InternalUserQueryController {

    private final UserQueryService userQueryService;

    @Operation(
            summary = "역할 기준 활성 사용자 목록 조회 (내부 전용)",
            description = "특정 role + userStatus 조건의 사용자 목록을 플랫 리스트로 반환. 팀/부서 필터 선택 가능."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "X-Internal-Token 누락/불일치")
    })
    @GetMapping("/by-role")
    public ResponseEntity<List<UserListResponse>> getUsersByRole(
            @Parameter(description = "사용자 역할 (admin/sales/production/shipping)", required = true)
            @RequestParam(name = "role") String role,
            @Parameter(description = "사용자 상태 필터 DB 값 (기본 active)")
            @RequestParam(name = "userStatus", required = false, defaultValue = "active") String userStatus,
            @Parameter(description = "팀 ID 필터 (선택)")
            @RequestParam(name = "teamId", required = false) Integer teamId,
            @Parameter(description = "부서 ID 필터 (선택 — 팀 경유)")
            @RequestParam(name = "departmentId", required = false) Integer departmentId,
            @Parameter(description = "최대 반환 수")
            @RequestParam(name = "limit", required = false, defaultValue = "200") int limit) {
        PagedResponse<UserListResponse> page =
                userQueryService.getUsers(null, teamId, departmentId, role, userStatus, 0, limit);

        List<UserListResponse> filtered = page.content().stream()
                .filter(u -> u.getUserEmail() != null && !u.getUserEmail().isBlank())
                .toList();

        return ResponseEntity.ok(filtered);
    }

    @Operation(
            summary = "결재자 후보 조회 (내부 전용)",
            description = "해당 팀의 팀장(position_level=1) + 전체 ADMIN 사용자 반환. teamId 미지정 시 전 팀의 팀장."
    )
    @GetMapping("/approvers")
    public ResponseEntity<List<UserListResponse>> getApprovers(
            @Parameter(description = "팀 ID (요청자 팀). 미지정 시 전체 팀장")
            @RequestParam(name = "teamId", required = false) Integer teamId) {
        return ResponseEntity.ok(userQueryService.getApprovers(teamId));
    }

    @Operation(
            summary = "팀 소속 사용자 ID 목록 (내부 전용)",
            description = "Documents 서비스의 PI/PO 팀 스코프 필터에 사용. active 상태만."
    )
    @GetMapping("/team/{teamId}/ids")
    public ResponseEntity<List<Integer>> getTeamMemberIds(
            @Parameter(description = "팀 ID", required = true)
            @PathVariable("teamId") Integer teamId) {
        return ResponseEntity.ok(userQueryService.findUserIdsByTeam(teamId));
    }
}
