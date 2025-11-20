package com.lqq.lqqaiagent.config;

import com.lqq.lqqaiagent.config.interceptor.UserContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserContextInterceptor userContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/favicon.ico")
                .excludePathPatterns("/user/login", "/user/register", "/user/logout")
                .excludePathPatterns("/chat/**")  // 排除聊天相关路径，允许公开访问
                .excludePathPatterns("/crawler/**", "/test.html")
                .excludePathPatterns("/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**")  // 排除API文档相关路径
                .excludePathPatterns("/error", "/actuator/**")  // 排除错误页面和监控端点
                .excludePathPatterns("/agent/**", "/codegen/**")  // 排除Agent和代码生成相关路径
                .excludePathPatterns("/streaming-chat-test.html");  // 排除流式聊天测试页面
    }
}
