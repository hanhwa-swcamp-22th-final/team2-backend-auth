package com.team2.auth.command.application.dto;

import com.team2.auth.command.domain.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @Schema(description = "이메일", example = "hong@example.com")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    private String email;

    @Schema(description = "비밀번호", example = "password123")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @Schema(description = "사용자 역할", example = "SALES")
    @NotNull(message = "사용자 권한을 확인해주세요.")
    private Role role;

    @Schema(description = "팀 ID", example = "1")
    @NotNull(message = "팀을 선택해주세요.")
    private Integer teamId;

    @Schema(description = "직급 ID", example = "2")
    @NotNull(message = "직급을 선택해주세요.")
    private Integer positionId;
}
