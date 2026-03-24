package com.team2.auth.dto;

import com.team2.auth.entity.enums.Role;
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
