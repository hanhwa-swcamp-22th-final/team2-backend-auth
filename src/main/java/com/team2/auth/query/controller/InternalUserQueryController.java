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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 서비스 간 내부 호출 전용 사용자 조회 API.
 * 인증: X-Internal-Token 헤더 (InternalApiTokenFilter 가 검증).
 * Gateway 에서 /api/**\/internal/** 경로를 denyAll 로 외부 차단한다.
 *
 * 현재 호출자: Documents 서비스 (AutoEmailRecipientResolver — 메일 수신자 자동 해소)
 */
@Tag(name = "사용자 내부 조회", description = "서비스 간 시스템 호출 전용. X-Internal-Token 필요")
@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class InternalUserQueryController {

    private final UserQueryService userQueryService;

    @Operation(
            summary = "역할 기준 활성 사용자 목록 조회 (내부 전용)",
            description = "특정 role + userStatus 조건의 사용자 목록을 플랫 리스트로 반환. HATEOAS 없이 순수 List."
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
            @Parameter(description = "최대 반환 수")
            @RequestParam(name = "limit", required = false, defaultValue = "200") int limit) {
        // findByCondition 을 재사용 — 내부 호출이므로 단일 페이지(size=limit) 로 조회
        PagedResponse<UserListResponse> page =
                userQueryService.getUsers(null, null, role, userStatus, 0, limit);

        // email 이 blank 인 항목 필터링
        List<UserListResponse> filtered = page.getContent().stream()
                .filter(u -> u.getUserEmail() != null && !u.getUserEmail().isBlank())
                .toList();

        return ResponseEntity.ok(filtered);
    }
}
