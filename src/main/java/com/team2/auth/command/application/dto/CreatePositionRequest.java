package com.team2.auth.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "직급 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePositionRequest {
    @Schema(description = "직급명", example = "팀장")
    @NotBlank(message = "직급명을 입력해주세요.")
    private String name;

    @Schema(description = "직급 레벨 (팀장=1, 팀원=3)", example = "1")
    @Min(value = 1, message = "직급 레벨은 1 이상이어야 합니다.")
    private int level;
}
