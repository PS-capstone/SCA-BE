package com.example.sca_be.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class CorsConfig implements WebMvcConfigurer{
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 일단 배포 문서에 작성되어 있는 사이트에 대해 작성
                .allowedOrigins(
                        "https://app.sca.site",
                        "https://teacher.sca.site",
                        "http://localhost:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)//자격증명 허용
                .maxAge(3600);//preflight 요청 생략 시간
    }
}
