package com.lqq.lqqaiagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // 禁用 CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // 启用 CORS
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())  // 允许所有请求
                .httpBasic(httpBasic -> httpBasic.disable())  // 禁用 HTTP Basic
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // 需要时创建 Session
                );

        return http.build();
    }

    /**
     * CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));  // 允许所有域名
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));  // 允许的方法
        configuration.setAllowedHeaders(Arrays.asList("*"));  // 允许所有请求头
        configuration.setExposedHeaders(Arrays.asList("*"));  // 暴露所有响应头
        configuration.setAllowCredentials(true);  // 允许携带凭证（Cookie）
        configuration.setMaxAge(3600L);  // 预检请求缓存时间

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 对所有路径生效
        return source;
    }
}
