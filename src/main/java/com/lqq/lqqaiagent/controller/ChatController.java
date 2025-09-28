package com.lqq.lqqaiagent.controller;

import com.lqq.lqqaiagent.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天控制器 - 接口层
 * 只负责接收HTTP请求和返回响应，业务逻辑委托给Service层处理
 * 这是正确的分层架构实现：
 * - Controller层：处理HTTP请求/响应，参数验证，异常处理
 * - Service层：处理业务逻辑，调用AI模型，管理会话
 * - Repository层：数据持久化操作
 *
 * @author LQQ
 * @since 2025-09-23
 */
@Slf4j
@RequestMapping("/test/chat")
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 带记忆功能的聊天接口
     * 支持多用户会话，每个用户的对话历史会被独立保存和管理
     * 
     * @param userId 用户ID，用于区分不同用户的会话
     * @param message 用户输入的消息内容
     * @return 统一格式的响应结果
     */
    @PostMapping("/memory")
    public Map<String, Object> chatWithMemory(
            @RequestParam String userId, 
            @RequestParam String message) {
        
        log.info("接收到用户 {} 的聊天请求: {}", userId, message);
        
        // 参数验证
        if (userId == null || userId.trim().isEmpty()) {
            return createErrorResponse("用户ID不能为空");
        }
        if (message == null || message.trim().isEmpty()) {
            return createErrorResponse("消息内容不能为空");
        }
        
        try {
            // 委托给Service层处理业务逻辑
            String response = chatService.chatWithMemory(message, userId);
            return createSuccessResponse(response, userId);
            
        } catch (Exception e) {
            log.error("处理聊天请求时发生错误", e);
            return createErrorResponse("处理请求时发生错误，请稍后重试");
        }
    }

    /**
     * 简单聊天接口（无记忆功能）
     * 每次对话都是独立的，不会保存或使用历史对话记录
     * 
     * @param message 用户输入的消息内容
     * @return 统一格式的响应结果
     */
    @PostMapping("/simple")
    public Map<String, Object> simpleChat(@RequestParam String message) {
        
        log.info("接收到简单聊天请求: {}", message);
        
        // 参数验证
        if (message == null || message.trim().isEmpty()) {
            return createErrorResponse("消息内容不能为空");
        }
        
        try {
            // 委托给Service层处理业务逻辑
            String response = chatService.simpleChat(message);
            return createSuccessResponse(response, null);
            
        } catch (Exception e) {
            log.error("处理简单聊天请求时发生错误", e);
            return createErrorResponse("处理请求时发生错误，请稍后重试");
        }
    }

    /**
     * 生成编程报告接口
     * 返回结构化的编程建议和总结
     *
     * @param userId 用户ID
     * @param message 用户输入的消息内容
     * @return 包含编程报告的响应结果
     */
    @PostMapping("/report")
    public Map<String, Object> generateReport(
            @RequestParam String userId,
            @RequestParam String message) {

        log.info("接收到用户 {} 的报告生成请求: {}", userId, message);

        // 参数验证
        if (userId == null || userId.trim().isEmpty()) {
            return createErrorResponse("用户ID不能为空");
        }
        if (message == null || message.trim().isEmpty()) {
            return createErrorResponse("消息内容不能为空");
        }

        try {
            // 委托给Service层处理业务逻辑
            ChatService.CodingReport report = chatService.generateCodingReport(message, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "报告生成成功");
            result.put("userId", userId);
            result.put("report", report);
            result.put("timestamp", System.currentTimeMillis());

            return result;

        } catch (Exception e) {
            log.error("生成编程报告时发生错误", e);
            return createErrorResponse("生成报告时发生错误，请稍后重试");
        }
    }

    /**
     * RAG聊天接口（检索增强生成）
     * 结合向量数据库进行知识检索，提供更准确的专业回答
     *
     * @param chatId 会话ID
     * @param message 用户输入的消息内容
     * @return 基于知识库增强的AI回复
     */
    @PostMapping("/rag")
    public Map<String, Object> chatWithRag(
            @RequestParam String chatId,
            @RequestParam String message) {

        log.info("接收到会话 {} 的RAG聊天请求: {}", chatId, message);

        // 参数验证
        if (chatId == null || chatId.trim().isEmpty()) {
            return createErrorResponse("会话ID不能为空");
        }
        if (message == null || message.trim().isEmpty()) {
            return createErrorResponse("消息内容不能为空");
        }

        try {
            // 委托给Service层处理RAG业务逻辑
//            String response = chatService.chatWithRag(message, chatId);
            //使用云服务RAG知识库来处理业务逻辑
            String response = chatService.chatWithRagCloud(message, chatId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "RAG聊天处理成功");
            result.put("chatId", chatId);
            result.put("content", response);
            result.put("type", "rag_enhanced");
            result.put("timestamp", System.currentTimeMillis());

            return result;

        } catch (Exception e) {
            log.error("处理RAG聊天请求时发生错误", e);
            return createErrorResponse("处理RAG聊天时发生错误，请稍后重试");
        }
    }

    /**
     * 获取用户聊天历史统计信息
     * 
     * @param userId 用户ID
     * @return 聊天历史统计信息
     */
    @GetMapping("/history/stats")
    public Map<String, Object> getChatStats(@RequestParam String userId) {
        
        log.info("获取用户 {} 的聊天统计信息", userId);
        
        if (userId == null || userId.trim().isEmpty()) {
            return createErrorResponse("用户ID不能为空");
        }
        
        try {
            int historyCount = chatService.getChatHistoryCount(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userId", userId);
            result.put("historyCount", historyCount);
            result.put("timestamp", System.currentTimeMillis());
            
            return result;
            
        } catch (Exception e) {
            log.error("获取聊天统计信息时发生错误", e);
            return createErrorResponse("获取统计信息时发生错误");
        }
    }

    /**
     * 清除用户聊天历史
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/history")
    public Map<String, Object> clearChatHistory(@RequestParam String userId) {
        
        log.info("清除用户 {} 的聊天历史", userId);
        
        if (userId == null || userId.trim().isEmpty()) {
            return createErrorResponse("用户ID不能为空");
        }
        
        try {
            chatService.clearChatHistory(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "聊天历史已清除");
            result.put("userId", userId);
            result.put("timestamp", System.currentTimeMillis());
            
            return result;
            
        } catch (Exception e) {
            log.error("清除聊天历史时发生错误", e);
            return createErrorResponse("清除历史时发生错误");
        }
    }

    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String content, String userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "请求处理成功");
        result.put("content", content);
        result.put("timestamp", System.currentTimeMillis());
        if (userId != null) {
            result.put("userId", userId);
        }
        return result;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", errorMessage);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
