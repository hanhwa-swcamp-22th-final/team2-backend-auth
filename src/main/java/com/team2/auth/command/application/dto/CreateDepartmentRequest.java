package com.team2.auth.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "부서 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartmentRequest {
    @Schema(description = "부서명", example = "개발팀")
    private String name;
}
