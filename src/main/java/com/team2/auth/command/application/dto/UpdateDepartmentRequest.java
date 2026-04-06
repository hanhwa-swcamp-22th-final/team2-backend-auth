package com.team2.auth.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "부서 수정 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDepartmentRequest {
    @Schema(description = "부서명", example = "마케팅팀")
    private String name;
}
