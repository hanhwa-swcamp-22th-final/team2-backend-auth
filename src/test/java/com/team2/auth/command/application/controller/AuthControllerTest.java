package com.team2.auth.command.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.command.application.dto.LoginRequest;
import com.team2.auth.command.application.dto.LogoutRequest;
import com.team2.auth.command.application.dto.RefreshRequest;
import com.team2.auth.query.dto.TokenResponse;
import com.team2.auth.command.application.service.AuthService;
import com.team2.auth.security.JwtProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@WithMockUser
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtProvider jwtProvider;

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

    @Test
    @DisplayName("GET /api/auth/validate - 유효한 토큰이면 200과 사용자 정보 헤더를 반환한다")
    void validate_success() throws Exception {
        // given
        String token = "valid-jwt-token";
        given(jwtProvider.validateAccessToken(token)).willReturn(true);
        Claims claims = Mockito.mock(Claims.class);
        given(claims.getSubject()).willReturn("1");
        given(claims.get("email", String.class)).willReturn("hong@test.com");
        given(claims.get("name", String.class)).willReturn("홍길동");
        given(claims.get("role", String.class)).willReturn("SALES");
        given(claims.get("departmentId")).willReturn(1);
        given(jwtProvider.parseAccessToken(token)).willReturn(claims);

        // when & then
        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("X-User-Id", "1"))
                .andExpect(header().string("X-User-Email", "hong@test.com"))
                .andExpect(header().string("X-User-Name", "홍길동"))
                .andExpect(header().string("X-User-Role", "SALES"))
                .andExpect(header().string("X-User-Department-Id", "1"));
    }

    @Test
    @DisplayName("GET /api/auth/validate - Authorization 헤더 없으면 401을 반환한다")
    void validate_noHeader() throws Exception {
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/auth/validate - 유효하지 않은 토큰이면 401을 반환한다")
    void validate_invalidToken() throws Exception {
        // given
        String token = "invalid-token";
        given(jwtProvider.validateAccessToken(token)).willReturn(false);

        // when & then
        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
