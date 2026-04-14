package com.team2.auth.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "팀 생성 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {
    @NotBlank
    @Schema(description = "팀 이름", example = "영업1팀")
    private String teamName;

    @NotNull
    @Schema(description = "소속 부서 ID", example = "1")
    private Integer departmentId;
}
