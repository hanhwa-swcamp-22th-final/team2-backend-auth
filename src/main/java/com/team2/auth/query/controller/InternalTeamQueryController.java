package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.Team;
import com.team2.auth.query.service.TeamQueryService;
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
 * 서비스 간 내부 호출 전용 팀 조회 API.
 * 인증: X-Internal-Token (InternalApiTokenFilter).
 * Gateway 에서 /api/**\/internal/** 경로를 denyAll 로 외부 차단한다.
 *
 * 호출자: Master 서비스 ClientQueryService — 거래처 리스트 enrich 용.
 */
@Tag(name = "팀 내부 조회", description = "서비스 간 시스템 호출 전용. X-Internal-Token 필요")
@RestController
@RequestMapping("/api/teams/internal")
@RequiredArgsConstructor
public class InternalTeamQueryController {

    private final TeamQueryService teamQueryService;

    @Operation(summary = "팀 ID 리스트 기반 일괄 조회 (내부 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "X-Internal-Token 누락/불일치")
    })
    @GetMapping("/by-ids")
    public ResponseEntity<List<TeamBrief>> getTeamsByIds(
            @Parameter(description = "팀 ID (쉼표 구분)", required = true)
            @RequestParam("ids") List<Integer> ids) {
        List<TeamBrief> body = ids.stream()
                .distinct()
                .map(id -> {
                    try { return teamQueryService.getTeam(id); }
                    catch (Exception e) { return null; }
                })
                .filter(java.util.Objects::nonNull)
                .map(this::toBrief)
                .toList();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "부서 소속 팀 목록 조회 (내부 전용)")
    @GetMapping("/by-department")
    public ResponseEntity<List<TeamBrief>> getTeamsByDepartment(
            @Parameter(description = "부서 ID", required = true)
            @RequestParam("departmentId") Integer departmentId) {
        List<TeamBrief> body = teamQueryService.getTeamsByDepartment(departmentId).stream()
                .map(this::toBrief)
                .toList();
        return ResponseEntity.ok(body);
    }

    private TeamBrief toBrief(Team t) {
        var d = t.getDepartment();
        return new TeamBrief(
                t.getTeamId(),
                t.getTeamName(),
                d != null ? d.getDepartmentId() : null,
                d != null ? d.getDepartmentName() : null
        );
    }

    public record TeamBrief(
            Integer teamId,
            String teamName,
            Integer departmentId,
            String departmentName
    ) {}
}
