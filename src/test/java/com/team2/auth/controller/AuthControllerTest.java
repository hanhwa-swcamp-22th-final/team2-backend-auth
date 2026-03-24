package com.team2.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.dto.LoginRequest;
import com.team2.auth.dto.LogoutRequest;
import com.team2.auth.dto.RefreshRequest;
import com.team2.auth.dto.TokenResponse;
import com.team2.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@WithMockUser
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login - 로그인 성공 시 토큰을 반환한다")
    void login_success() throws Exception {
        // given
        LoginRequest request = new LoginRequest("hong@test.com", "password");
        TokenResponse response = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();
        given(authService.login("hong@test.com", "password")).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - 토큰 갱신 성공")
    void refresh_success() throws Exception {
        // given
        RefreshRequest request = new RefreshRequest("old-refresh-token");
        TokenResponse response = TokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();
        given(authService.refreshToken("old-refresh-token")).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("POST /api/auth/logout - 로그아웃 성공")
    void logout_success() throws Exception {
        // given
        LogoutRequest request = new LogoutRequest(1);

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).logout(1);
    }
}
