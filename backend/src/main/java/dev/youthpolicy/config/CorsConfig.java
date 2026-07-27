package dev.youthpolicy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ADR-0004 D2 — CORS는 프론트 오리진으로 제한한다(PRD 9장 프라이버시 제약).
 * 배포 오리진은 미정(ADR-0004 D6)이라 프로퍼티로 주입, 기본값은 로컬 개발용 Next.js 포트.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origin:http://localhost:3000}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/verdicts")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("POST");
    }
}
