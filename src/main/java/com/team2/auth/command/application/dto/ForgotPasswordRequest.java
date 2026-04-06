package com.team2.auth.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "비밀번호 찾기 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {
    @Schema(description = "비밀번호를 찾을 사용자 이메일", example = "user@example.com")
    private String email;
}
