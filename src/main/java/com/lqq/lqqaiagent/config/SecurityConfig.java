package com.lqq.lqqaiagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ✅ 允许不登录就能访问的接口
                .requestMatchers(
                    "/user/register",      // 注册接口放行
                    "/user/login",         // 登录接口放行（如果有的话）
                    "/swagger-ui/**",      // Swagger UI
                    "/swagger-ui.html",    // 旧版 Swagger UI
                    "/v3/api-docs/**",     // OpenAPI 文档
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
            )
            // ✅ 关闭 CSRF（否则 POST /user/register 会报 403）
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
