package com.team2.auth.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로그아웃 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    @Schema(description = "로그아웃할 사용자 ID", example = "1")
    private Integer userId;
}
