package com.team2.auth.config;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * auth 서비스 공통 예외 → HTTP status 매핑.
 * IllegalArgumentException (잘못된 입력/찾을 수 없음) → 401
 * IllegalStateException (로그인 불가 상태 등) → 403
 * 프론트가 e.response.status 로 정확히 분기할 수 있도록 500 (Spring 기본) 을 막는다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "잘못된 요청입니다."));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "요청을 처리할 수 없는 상태입니다."));
    }
}
