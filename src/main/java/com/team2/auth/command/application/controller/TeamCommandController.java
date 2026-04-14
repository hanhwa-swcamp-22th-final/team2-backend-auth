package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.CreateTeamRequest;
import com.team2.auth.command.application.dto.UpdateTeamRequest;
import com.team2.auth.command.application.service.TeamCommandService;
import com.team2.auth.command.domain.entity.Team;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "팀 관리 (Command)", description = "팀 생성, 수정, 삭제 API")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamCommandController {

    private final TeamCommandService teamCommandService;

    @Operation(summary = "팀 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        Team team = teamCommandService.createTeam(request);
        return ResponseEntity.status(201).body(toDto(team));
    }

    @Operation(summary = "팀 수정")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTeam(
            @Parameter(description = "팀 ID") @PathVariable("id") Integer id,
            @RequestBody UpdateTeamRequest request) {
        Team team = teamCommandService.updateTeam(id, request);
        return ResponseEntity.ok(toDto(team));
    }

    @Operation(summary = "팀 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(
            @Parameter(description = "팀 ID") @PathVariable("id") Integer id) {
        teamCommandService.deleteTeam(id);
        return ResponseEntity.noContent().build();
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
