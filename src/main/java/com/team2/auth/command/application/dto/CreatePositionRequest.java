package com.team2.auth.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "직급 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePositionRequest {
    @Schema(description = "직급명", example = "과장")
    private String name;

    @Schema(description = "직급 레벨 (숫자가 클수록 상위)", example = "3")
    private int level;
}
