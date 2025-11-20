package com.lqq.lqqaiagent.controller;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 聊天控制器简单测试类 - 不依赖 Spring 容器
 * 
 * @author lqq
 * @date 2025-11-20
 */
@Slf4j
public class ChatControllerSimpleTest {

    @Mock
    private ChatModel chatModel;
    
    @Mock
    private StreamingChatModel streamingChatModel;

    /**
     * 测试控制器基本功能
     */
    @Test
    public void testControllerBasics() {
        MockitoAnnotations.openMocks(this);
        
        log.info("=== 聊天控制器基本功能测试 ===");
        
        // 创建控制器实例
        ChatController controller = new ChatController();
        
        // 测试健康检查方法
        Map<String, Object> health = controller.health();
        
        log.info("健康检查结果: {}", health);
        
        // 验证基本字段
        assert health.containsKey("status");
        assert health.containsKey("timestamp");
        assert health.containsKey("chatModel");
        assert health.containsKey("streamingChatModel");
        
        log.info("✅ 控制器基本功能测试通过");
    }

    /**
     * 测试流式聊天功能的可用性
     */
    @Test
    public void testStreamingChatAvailability() {
        log.info("=== 流式聊天功能可用性测试 ===");
        
        // 检查流式聊天相关的类是否可以正常加载
        try {
            Class<?> streamingChatModelClass = StreamingChatModel.class;
            Class<?> chatModelClass = ChatModel.class;
            
            log.info("✅ StreamingChatModel 类加载成功: {}", streamingChatModelClass.getName());
            log.info("✅ ChatModel 类加载成功: {}", chatModelClass.getName());
            
            // 检查控制器类
            Class<?> controllerClass = ChatController.class;
            log.info("✅ ChatController 类加载成功: {}", controllerClass.getName());
            
            // 检查控制器中的方法
            boolean hasStreamMethod = false;
            boolean hasSimpleStreamMethod = false;
            boolean hasHealthMethod = false;
            
            for (var method : controllerClass.getDeclaredMethods()) {
                if ("streamChat".equals(method.getName())) {
                    hasStreamMethod = true;
                }
                if ("streamChatSimple".equals(method.getName())) {
                    hasSimpleStreamMethod = true;
                }
                if ("health".equals(method.getName())) {
                    hasHealthMethod = true;
                }
            }
            
            log.info("流式聊天方法存在: {}", hasStreamMethod ? "✅" : "❌");
            log.info("简单流式聊天方法存在: {}", hasSimpleStreamMethod ? "✅" : "❌");
            log.info("健康检查方法存在: {}", hasHealthMethod ? "✅" : "❌");
            
            assert hasStreamMethod : "streamChat 方法不存在";
            assert hasSimpleStreamMethod : "streamChatSimple 方法不存在";
            assert hasHealthMethod : "health 方法不存在";
            
            log.info("✅ 流式聊天功能可用性测试通过");
            
        } catch (Exception e) {
            log.error("❌ 流式聊天功能可用性测试失败", e);
            throw e;
        }
    }

    /**
     * 测试 langchain4j 依赖
     */
    @Test
    public void testLangChain4jDependencies() {
        log.info("=== LangChain4j 依赖测试 ===");
        
        try {
            // 检查核心类
            Class.forName("dev.langchain4j.model.chat.ChatModel");
            log.info("✅ ChatModel 类可用");
            
            Class.forName("dev.langchain4j.model.chat.StreamingChatModel");
            log.info("✅ StreamingChatModel 类可用");
            
            Class.forName("dev.langchain4j.model.chat.response.StreamingChatResponseHandler");
            log.info("✅ StreamingChatResponseHandler 类可用");
            
            Class.forName("dev.langchain4j.model.chat.request.ChatRequest");
            log.info("✅ ChatRequest 类可用");
            
            Class.forName("dev.langchain4j.model.chat.response.ChatResponse");
            log.info("✅ ChatResponse 类可用");
            
            Class.forName("dev.langchain4j.data.message.AiMessage");
            log.info("✅ AiMessage 类可用");
            
            Class.forName("dev.langchain4j.data.message.UserMessage");
            log.info("✅ UserMessage 类可用");
            
            log.info("✅ LangChain4j 依赖测试通过");
            
        } catch (ClassNotFoundException e) {
            log.error("❌ LangChain4j 依赖测试失败: {}", e.getMessage());
            throw new RuntimeException("LangChain4j 依赖不完整", e);
        }
    }
}
