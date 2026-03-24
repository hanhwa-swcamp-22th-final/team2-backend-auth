package com.team2.auth.dto;

import com.team2.auth.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusRequest {
    private UserStatus status;
}
