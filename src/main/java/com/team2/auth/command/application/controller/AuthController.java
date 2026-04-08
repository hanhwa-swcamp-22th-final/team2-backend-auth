package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.ForgotPasswordRequest;
import com.team2.auth.command.application.dto.LoginRequest;
import com.team2.auth.command.application.dto.LogoutRequest;
import com.team2.auth.query.dto.TokenResponse;
import com.team2.auth.command.application.service.AuthService;
import com.team2.auth.command.application.service.UserCommandService;
import com.team2.auth.security.JwtProvider;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "로그인, 로그아웃, 토큰 갱신 등 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String COOKIE_NAME = "sb_refresh_token";

    private final AuthService authService;
    private final UserCommandService userCommandService;
    private final JwtProvider jwtProvider;

    @Value("${auth.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${auth.cookie.same-site:Strict}")
    private String cookieSameSite;

    @Value("${jwt.refresh-token-expiry:604800000}")
    private long refreshTokenExpiry;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 액세스 토큰을 발급받습니다. 리프레시 토큰은 HttpOnly 쿠키로 설정됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 불일치)")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request.getEmail(), request.getPassword());
        ResponseCookie cookie = buildRefreshTokenCookie(response.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @Operation(summary = "토큰 갱신", description = "sb_refresh_token 쿠키로 새로운 액세스 토큰을 발급받습니다. 리프레시 토큰도 회전 발급됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "쿠키 없음 또는 유효하지 않은 리프레시 토큰")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TokenResponse response = authService.refreshToken(refreshToken);
        ResponseCookie cookie = buildRefreshTokenCookie(response.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @Operation(summary = "로그아웃", description = "사용자의 리프레시 토큰을 삭제하고 쿠키를 만료시킵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.getUserId());
        ResponseCookie cookie = buildExpiredCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @Operation(summary = "비밀번호 찾기", description = "이메일로 임시 비밀번호를 발송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "임시 비밀번호 발송 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "해당 이메일의 사용자를 찾을 수 없음")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userCommandService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "토큰 검증", description = "Authorization 헤더의 Bearer 토큰을 검증하고 사용자 정보를 응답 헤더에 포함합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 검증 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @GetMapping("/validate")
    public ResponseEntity<Void> validate(
            @Parameter(description = "Bearer 액세스 토큰", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        if (!jwtProvider.validateAccessToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Claims claims = jwtProvider.parseAccessToken(token);
        return ResponseEntity.ok()
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Email", claims.get("email", String.class))
                .header("X-User-Name", claims.get("name", String.class))
                .header("X-User-Role", claims.get("role", String.class))
                .header("X-User-Department-Id", String.valueOf(claims.get("departmentId")))
                .build();
    }

    private ResponseCookie buildRefreshTokenCookie(String token) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(refreshTokenExpiry / 1000)
                .build();
    }

    private ResponseCookie buildExpiredCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}
