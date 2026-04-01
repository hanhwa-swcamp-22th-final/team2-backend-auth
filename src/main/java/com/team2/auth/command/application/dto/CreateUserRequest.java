package com.team2.auth.command.application.dto;

import com.team2.auth.command.domain.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private String employeeNo;
    private String name;
    private String email;
    private String password;
    private Role role;
}
