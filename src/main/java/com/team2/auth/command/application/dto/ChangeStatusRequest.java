package com.team2.auth.command.application.dto;

import com.team2.auth.command.domain.entity.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용자 상태 변경 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusRequest {
    @Schema(description = "변경할 사용자 상태", example = "ACTIVE")
    private UserStatus status;
}
