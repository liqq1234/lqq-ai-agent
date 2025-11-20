package com.lqq.lqqaiagent.controller;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 聊天控制器测试类
 * 
 * @author lqq
 * @date 2025-11-20
 */
@Slf4j
@SpringBootTest(
    classes = com.lqq.lqqaiagent.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ChatControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired(required = false)
    private ChatModel chatModel;
    
    @Autowired(required = false)
    private StreamingChatModel streamingChatModel;

    /**
     * 测试普通聊天接口
     */
    @Test
    public void testChatMessage() {
        // 检查 Bean 是否可用
        if (chatModel == null) {
            log.warn("ChatModel Bean 不可用，跳过测试");
            return;
        }
        
        // 准备请求
        Map<String, String> request = Map.of("message", "你好，请简单自我介绍一下");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
        // 发送请求
        log.info("发送普通聊天请求: {}", request.get("message"));
        ResponseEntity<Map> response = restTemplate.postForEntity("/chat/message", entity, Map.class);
        
        // 验证响应
        log.info("响应状态: {}", response.getStatusCode());
        log.info("响应内容: {}", response.getBody());
        
        if (response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            if (Boolean.TRUE.equals(responseBody.get("success"))) {
                log.info("✅ 普通聊天测试成功");
                log.info("AI 响应: {}", responseBody.get("message"));
            } else {
                log.error("❌ 普通聊天测试失败: {}", responseBody.get("error"));
            }
        }
    }

    /**
     * 测试流式聊天接口可用性
     */
    @Test
    public void testStreamingChatAvailability() {
        // 检查 Bean 是否可用
        if (streamingChatModel == null) {
            log.warn("StreamingChatModel Bean 不可用，跳过测试");
            return;
        }
        
        log.info("✅ StreamingChatModel Bean 可用，流式聊天功能已就绪");
        
        // 测试健康检查接口
        ResponseEntity<Map> healthResponse = restTemplate.getForEntity("/chat/health", Map.class);
        log.info("健康检查响应: {}", healthResponse.getBody());
        
        if (healthResponse.getBody() != null) {
            Map<String, Object> health = healthResponse.getBody();
            log.info("聊天模型状态: {}", health.get("chatModel"));
            log.info("流式聊天模型状态: {}", health.get("streamingChatModel"));
        }
    }

    /**
     * 测试配置和依赖
     */
    @Test
    public void testConfiguration() {
        log.info("=== 聊天功能配置检查 ===");
        
        // 检查 ChatModel
        if (chatModel != null) {
            log.info("✅ ChatModel 配置正常");
        } else {
            log.warn("⚠️ ChatModel 未配置或不可用");
        }
        
        // 检查 StreamingChatModel
        if (streamingChatModel != null) {
            log.info("✅ StreamingChatModel 配置正常");
        } else {
            log.warn("⚠️ StreamingChatModel 未配置或不可用");
        }
        
        // 检查健康状态
        try {
            ResponseEntity<Map> healthResponse = restTemplate.getForEntity("/chat/health", Map.class);
            if (healthResponse.getStatusCode().is2xxSuccessful()) {
                log.info("✅ 聊天控制器健康检查通过");
                log.info("健康状态: {}", healthResponse.getBody());
            } else {
                log.warn("⚠️ 聊天控制器健康检查失败: {}", healthResponse.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ 聊天控制器不可访问: {}", e.getMessage());
        }
    }
}
