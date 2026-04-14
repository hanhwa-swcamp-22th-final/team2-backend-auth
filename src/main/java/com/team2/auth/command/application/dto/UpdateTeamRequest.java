package com.team2.auth.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "팀 정보 수정 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeamRequest {
    @Schema(description = "팀 이름", example = "영업1팀")
    private String teamName;

    @Schema(description = "부서 이동 시 새 부서 ID", example = "2")
    private Integer departmentId;
}
