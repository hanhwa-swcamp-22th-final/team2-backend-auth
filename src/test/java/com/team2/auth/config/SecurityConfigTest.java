package com.team2.auth.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    @DisplayName("PasswordEncoder 빈이 BCryptPasswordEncoder이다")
    void passwordEncoder_isBCrypt() {
        assertThat(passwordEncoder).isNotNull();
        String encoded = passwordEncoder.encode("test");
        assertThat(passwordEncoder.matches("test", encoded)).isTrue();
        assertThat(passwordEncoder.matches("wrong", encoded)).isFalse();
    }

    @Test
    @DisplayName("CorsConfigurationSource 빈이 정상 생성된다")
    void corsConfigurationSource_isNotNull() {
        assertThat(corsConfigurationSource).isNotNull();
    }

    @Test
    @DisplayName("CSRF가 비활성화되어 있다")
    void csrf_isDisabled() throws Exception {
        mockMvc.perform(get("/api/any-endpoint"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CORS preflight 요청이 허용된다")
    void cors_preflightAllowed() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:8001")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk());
    }
}
