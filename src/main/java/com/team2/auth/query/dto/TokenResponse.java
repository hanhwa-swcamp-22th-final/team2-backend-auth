package com.team2.auth.query.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore  // RT는 HttpOnly 쿠키로 전달 — 응답 body에 포함하지 않음
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

        @Schema(description = "사번", example = "EMP001")
        private String employeeNo;

        @Schema(description = "사용자 이름", example = "홍길동")
        private String userName;

        @Schema(description = "이메일", example = "hong@example.com")
        private String userEmail;

        @Schema(description = "사용자 역할", example = "ADMIN")
        private String userRole;

        @Schema(description = "팀 ID", example = "1")
        private Integer teamId;

        @Schema(description = "팀 이름", example = "영업1팀")
        private String teamName;

        @Schema(description = "부서 ID", example = "1")
        private Integer departmentId;

        @Schema(description = "부서명", example = "영업부")
        private String departmentName;

        @Schema(description = "직급 ID", example = "1")
        private Integer positionId;

        @Schema(description = "직급 레벨 (1=팀장, 2=팀원)", example = "1")
        private Integer positionLevel;

        @Schema(description = "직급명", example = "과장")
        private String positionName;
    }
}
