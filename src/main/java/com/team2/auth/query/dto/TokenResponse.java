package com.team2.auth.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "토큰 응답")
@Getter
@Builder
@AllArgsConstructor
public class TokenResponse {
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "로그인 사용자 정보")
    private UserInfo user;

    @Schema(description = "로그인 사용자 요약 정보")
    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        @Schema(description = "사용자 ID", example = "1")
        private Integer userId;

        @Schema(description = "사용자 이름", example = "홍길동")
        private String userName;

        @Schema(description = "사용자 역할", example = "ADMIN")
        private String userRole;

        @Schema(description = "부서명", example = "개발팀")
        private String departmentName;

        @Schema(description = "직급명", example = "과장")
        private String positionName;
    }
}
