package com.team2.auth.config;

import com.team2.auth.security.JwtAuthFilter;
import com.team2.auth.security.JwtProvider;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:8001}")
    private String allowedOrigins;

    private final JwtProvider jwtProvider;
    private final InternalApiTokenFilter internalApiTokenFilter;

    public SecurityConfig(JwtProvider jwtProvider, InternalApiTokenFilter internalApiTokenFilter) {
        this.jwtProvider = jwtProvider;
        this.internalApiTokenFilter = internalApiTokenFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // InternalApiTokenFilter 는 JwtAuthFilter 보다 먼저 실행되어 /internal 경로의
            // X-Internal-Token 헤더를 검증한다. 시스템 호출(Documents → Auth 등)을 위한 것.
            .addFilterBefore(internalApiTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/logout",
                    "/api/auth/forgot-password",
                    "/.well-known/jwks.json",
                    "/actuator/health",
                    "/actuator/info",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/h2-console/**"
                ).permitAll()
                // /api/**\/internal/** 경로는 InternalApiTokenFilter 가 이미 X-Internal-Token 으로 검증했다.
                // Gateway 에서는 동일 경로를 denyAll 로 외부 차단하므로 여기서는 permitAll 이 안전하다.
                .requestMatchers("/api/users/internal/**").permitAll()
                .requestMatchers("/api/teams/internal/**").permitAll()
                // 뷰어 선택 등 일반 인증 사용자도 접근 가능한 제한 목록 (ADMIN 전용 CRUD 보다 앞)
                .requestMatchers(HttpMethod.GET, "/api/users/viewable").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/positions/**", "/api/departments/**", "/api/teams/**", "/api/company/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/positions/**", "/api/departments/**", "/api/teams/**", "/api/company/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/positions/**", "/api/departments/**", "/api/teams/**", "/api/company/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(
                    (req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage())
            ));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
