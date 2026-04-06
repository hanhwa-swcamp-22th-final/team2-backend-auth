package com.team2.auth.query.dto;

import com.team2.auth.command.domain.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용자 상세 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {

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

    @Schema(description = "부서명", example = "개발팀")
    private String departmentName;

    @Schema(description = "직급명", example = "과장")
    private String positionName;

    @Schema(description = "직급 정보 (서비스 간 통신용)")
    private PositionDetail position;

    @Schema(description = "사용자 상태", example = "ACTIVE")
    private String userStatus;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionDetail {
        @Schema(description = "직급 레벨 (1=팀장, 2=직원)", example = "1")
        private Integer positionLevel;
    }

    public static UserDetailResponse from(User user) {
        return UserDetailResponse.builder()
                .userId(user.getUserId())
                .employeeNo(user.getEmployeeNo())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .userRole(user.getUserRole() != null ? user.getUserRole().name() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null)
                .positionName(user.getPosition() != null ? user.getPosition().getPositionName() : null)
                .position(user.getPosition() != null
                        ? PositionDetail.builder().positionLevel(user.getPosition().getPositionLevel()).build()
                        : null)
                .userStatus(user.getUserStatus() != null ? user.getUserStatus().name() : null)
                .build();
    }
}
