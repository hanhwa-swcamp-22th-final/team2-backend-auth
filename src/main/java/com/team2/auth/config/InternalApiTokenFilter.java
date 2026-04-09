package com.team2.auth.config;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * /api/**\/internal/** 경로 진입 시 X-Internal-Token 헤더를 검증한다.
 * Activity 의 InternalApiTokenFilter 와 동일한 규약으로 대칭 구성.
 *
 * 토큰이 비어 있으면(개발 환경) 통과시키되 WARN 로그를 남기고,
 * 토큰이 설정되어 있으면 헤더 일치 여부를 검사한 뒤 불일치 시 403 반환.
 *
 * Gateway 에서 {@code /api/**\/internal/**} 경로를 denyAll 로 외부 차단하므로
 * 외부에서는 본 필터까지 도달 불가. 같은 docker network 내부 호출만 허용.
 */
@Component
public class InternalApiTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalApiTokenFilter.class);
    private static final String HEADER_NAME = "X-Internal-Token";

    private static final int MIN_TOKEN_LENGTH = 32;

    @Value("${internal.api.token:}")
    private String configuredToken;

    private final Environment environment;

    public InternalApiTokenFilter(Environment environment) {
        this.environment = environment;
    }

    /**
     * Fail-fast 검증 — 운영(prod) 프로파일에서 토큰이 비어있거나 너무 약하면 기동 실패.
     * k8s Secret 주입 실패 시 Pod 가 Ready 되지 않도록 한다.
     * dev/test 프로파일에서는 blank 를 허용 (WARN 로그만).
     */
    @PostConstruct
    void validateOnStartup() {
        boolean isProd = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("prod") || p.equalsIgnoreCase("production"));
        if (!isProd) {
            return;
        }
        if (configuredToken == null || configuredToken.isBlank()) {
            throw new IllegalStateException(
                    "INTERNAL_API_TOKEN must be set in prod profile. "
                            + "Check k8s Secret / docker env injection.");
        }
        if (configuredToken.length() < MIN_TOKEN_LENGTH) {
            throw new IllegalStateException(
                    "INTERNAL_API_TOKEN is too weak (length=" + configuredToken.length()
                            + ", min=" + MIN_TOKEN_LENGTH + "). "
                            + "Use 'openssl rand -hex 32' or equivalent.");
        }
        log.info("InternalApiTokenFilter: prod profile token validated (length={})", configuredToken.length());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && path.contains("/internal")) {
            if (configuredToken == null || configuredToken.isBlank()) {
                log.warn("internal.api.token 미설정 — {} 경로 검증 skip (개발 환경에서만 허용)", path);
            } else {
                String received = request.getHeader(HEADER_NAME);
                if (received == null || !configuredToken.equals(received)) {
                    log.warn("internal endpoint forbidden: missing/invalid {} header for {}", HEADER_NAME, path);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid internal token");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
