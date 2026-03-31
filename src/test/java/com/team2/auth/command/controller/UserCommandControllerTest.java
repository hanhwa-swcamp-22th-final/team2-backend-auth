package com.team2.auth.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.dto.ChangeStatusRequest;
import com.team2.auth.dto.CreateUserRequest;
import com.team2.auth.dto.UpdateUserRequest;
import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import com.team2.auth.command.service.UserCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserCommandController.class)
@WithMockUser
class UserCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserCommandService userCommandService;

    private User createTestUser() {
        return User.builder()
                .employeeNo("EMP001")
                .userName("홍길동")
                .userEmail("hong@test.com")
                .userPw("encodedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("POST /api/users - 사용자 생성 성공")
    void createUser_success() throws Exception {
        // given
        CreateUserRequest request = CreateUserRequest.builder()
                .employeeNo("EMP001")
                .name("홍길동")
                .email("hong@test.com")
                .password("password123")
                .role(Role.SALES)
                .build();
        given(userCommandService.createUser(any(CreateUserRequest.class))).willReturn(createTestUser());

        // when & then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("홍길동"))
                .andExpect(jsonPath("$.userEmail").value("hong@test.com"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - 사용자 수정 성공")
    void updateUser_success() throws Exception {
        // given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .email("kim@test.com")
                .build();
        User updatedUser = User.builder()
                .employeeNo("EMP001")
                .userName("김길동")
                .userEmail("kim@test.com")
                .userPw("encodedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.ACTIVE)
                .build();
        given(userCommandService.updateUser(eq(1), any(UpdateUserRequest.class))).willReturn(updatedUser);

        // when & then
        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("김길동"));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/status - 상태 변경 성공")
    void changeStatus_success() throws Exception {
        // given
        ChangeStatusRequest request = new ChangeStatusRequest(UserStatus.ON_LEAVE);
        User updatedUser = User.builder()
                .employeeNo("EMP001")
                .userName("홍길동")
                .userEmail("hong@test.com")
                .userPw("encodedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.ON_LEAVE)
                .build();
        given(userCommandService.changeStatus(eq(1), eq(UserStatus.ON_LEAVE))).willReturn(updatedUser);

        // when & then
        mockMvc.perform(patch("/api/users/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userStatus").value("ON_LEAVE"));
    }
}
