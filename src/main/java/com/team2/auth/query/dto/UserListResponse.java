package com.team2.auth.query.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserListResponse {
    private Integer userId;
    private String employeeNo;
    private String userName;
    private String userEmail;
    private String userRole;
    private String departmentName;
    private String positionName;
    private String userStatus;
    private LocalDateTime createdAt;
}
