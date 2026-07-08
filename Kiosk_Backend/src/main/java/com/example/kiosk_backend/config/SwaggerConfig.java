package com.example.kiosk_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    // Swagger UI 설정을 만들어서 스프링 빈으로 등록
    @Bean
    public OpenAPI openAPI() {
        // Swagger에서 JWT 토큰을 입력하는 방법을 Bearer 방식으로 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("bearerAuth")
                .description("토큰 값만 입력하세요(Bearer 없이)\n\n" +
                        "로그인 응답의 accessToken 값을 그대로 붙여주세요.\n\n" +
                        "Swagger가 자동으로 'Bearer ' 접두사를 붙여줍니다.\n\n" +
                        "예) fhjkl:asdsfafdsgfadgsfadsf..."
                );

        // 만든 보안 설정을 실제로 적용하겠다고 선언
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth"); // "bearerAuth"라는 이름의 보안 설정을 사용

        return new OpenAPI()
                // API 기본 정보 설정
                .info(new Info()
                        .title("Kiosk RESTFul API")
                        .description("버거킹 키오스크 백엔드 API 입니다.")
                        .version("1.0.0")
                )
                // 보안 스킴(bearerAuth) 등록
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                // 등록한 보안 스킴을 전체 API에 기본 적용
                .addSecurityItem(securityRequirement);
    }
}
