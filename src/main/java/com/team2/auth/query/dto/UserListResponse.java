package com.team2.auth.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "사용자 목록 응답 항목")
@Getter
@Setter
public class UserListResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Integer userId;

    @Schema(description = "사번", example = "EMP001")
    private String employeeNo;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String userName;

    @Schema(description = "이메일", example = "hong@example.com")
    private String userEmail;

    @Schema(description = "사용자 역할", example = "USER")
    private String userRole;

    @Schema(description = "팀 ID", example = "1")
    private Integer teamId;

    @Schema(description = "팀 이름", example = "영업1팀")
    private String teamName;

    @Schema(description = "부서 ID", example = "1")
    private Integer departmentId;

    @Schema(description = "부서 이름", example = "영업부")
    private String departmentName;

    @Schema(description = "직급명", example = "과장")
    private String positionName;

    @Schema(description = "사용자 상태", example = "ACTIVE")
    private String userStatus;

    @Schema(description = "생성일시", example = "2026-01-15T09:30:00")
    private LocalDateTime createdAt;
}
