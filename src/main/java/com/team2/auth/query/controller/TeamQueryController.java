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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "팀 조회", description = "팀 목록 조회 API (부서 하위 조직)")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamQueryController {

    private final TeamQueryService teamQueryService;

    @Operation(summary = "전체 팀 목록 조회")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "조회 성공") })
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllTeams(
            @Parameter(description = "부서 ID 필터 (선택)")
            @RequestParam(name = "departmentId", required = false) Integer departmentId) {
        List<Team> teams = departmentId != null
                ? teamQueryService.getTeamsByDepartment(departmentId)
                : teamQueryService.getAllTeams();
        List<Map<String, Object>> body = teams.stream().map(this::toDto).toList();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "팀 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTeam(
            @Parameter(description = "팀 ID") @PathVariable("id") Integer id) {
        return ResponseEntity.ok(toDto(teamQueryService.getTeam(id)));
    }

    private Map<String, Object> toDto(Team t) {
        var dept = t.getDepartment();
        return Map.of(
                "teamId", t.getTeamId(),
                "teamName", t.getTeamName(),
                "departmentId", dept != null ? dept.getDepartmentId() : null,
                "departmentName", dept != null ? dept.getDepartmentName() : null
        );
    }
}
