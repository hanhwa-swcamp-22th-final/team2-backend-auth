package com.team2.auth.command.application.dto;

import com.team2.auth.command.domain.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusRequest {
    private UserStatus status;
}
