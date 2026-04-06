package com.team2.auth.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "비밀번호 변경 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @Schema(description = "현재 비밀번호", example = "oldPassword123")
    private String currentPw;

    @Schema(description = "새 비밀번호", example = "newPassword456")
    private String newPw;
}
