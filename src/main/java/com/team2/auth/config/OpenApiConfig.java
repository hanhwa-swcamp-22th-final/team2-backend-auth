package com.team2.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .version("1.0")
                        .description("인증, 사용자, 부서, 직급, 회사 관리 API\n\n"
                                + "### Swagger 인증 방법\n"
                                + "1. `POST /api/auth/login` 실행 → 응답의 `accessToken` 복사\n"
                                + "2. 우측 상단 **Authorize** 🔓 버튼 클릭\n"
                                + "3. 값 입력란에 토큰만 붙여넣기 (Bearer 접두어 불필요)\n"
                                + "4. 이후 모든 API 호출에 JWT 자동 첨부"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Access Token (RS256). "
                                        + "POST /api/auth/login 응답의 accessToken 값을 입력하세요.")));
    }
}
