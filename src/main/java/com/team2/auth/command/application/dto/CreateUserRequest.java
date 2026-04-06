package com.team2.auth.command.application.dto;

import com.team2.auth.command.domain.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용자 생성 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일", example = "hong@example.com")
    private String email;

    @Schema(description = "비밀번호", example = "password123")
    private String password;

    @Schema(description = "사용자 역할", example = "USER")
    private Role role;
}
