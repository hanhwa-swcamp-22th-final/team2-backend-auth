package com.team2.auth.command.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.command.application.dto.ChangePasswordRequest;
import com.team2.auth.command.application.dto.ChangeStatusRequest;
import com.team2.auth.command.application.dto.CreateUserRequest;
import com.team2.auth.command.application.dto.UpdateUserRequest;
import com.team2.auth.command.domain.entity.User;
import com.team2.auth.command.domain.entity.enums.Role;
import com.team2.auth.command.domain.entity.enums.UserStatus;
import com.team2.auth.command.application.service.UserCommandService;
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
import static org.mockito.Mockito.verify;
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
                .name("홍길동")
                .email("hong@test.com")
                .password("password123")
                .role(Role.SALES)
                .build();
        User createdUser = createTestUser();
        java.lang.reflect.Field idField = User.class.getDeclaredField("userId");
        idField.setAccessible(true);
        idField.set(createdUser, 1);
        given(userCommandService.createUser(any(CreateUserRequest.class))).willReturn(createdUser);

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

    @Test
    @DisplayName("PUT /api/users/{id}/password - 비밀번호 변경 성공")
    void changePassword_success() throws Exception {
        // given
        ChangePasswordRequest request = new ChangePasswordRequest("currentPw", "newPw123");

        // when & then
        mockMvc.perform(put("/api/users/1/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userCommandService).changePassword(1, "currentPw", "newPw123");
    }

    @Test
    @DisplayName("POST /api/users/{id}/password/reset - 비밀번호 초기화 성공")
    void resetPassword_success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/users/1/password/reset")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(userCommandService).resetPassword(1);
    }
}
