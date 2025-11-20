package com.lqq.lqqaiagent.langchain4j;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天记忆管理工具
 * 使用LangChain4j原生的记忆管理
 * 
 * @author lqq
 * @date 2025-11-08
 */
@Slf4j
@Component
public class ChatMemoryTool {
    
    // 简单的内存存储，生产环境可以替换为Redis或数据库
    private final Map<String, List<String>> conversationHistory = new HashMap<>();
    
    @Tool("保存对话记录")
    public String saveConversation(
        @P("用户ID") String userId,
        @P("用户消息") String userMessage,
        @P("AI回复") String aiResponse
    ) {
        try {
            String conversationId = "user_" + userId;
            
            conversationHistory.computeIfAbsent(conversationId, k -> new ArrayList<>())
                .add(LocalDateTime.now() + " - 用户: " + userMessage);
            
            conversationHistory.get(conversationId)
                .add(LocalDateTime.now() + " - AI: " + aiResponse);
            
            log.info("保存对话记录成功，用户ID: {}", userId);
            return "✅ 对话记录已保存";
            
        } catch (Exception e) {
            log.error("保存对话记录失败", e);
            return "❌ 保存失败: " + e.getMessage();
        }
    }
    
    @Tool("获取对话历史")
    public String getConversationHistory(
        @P("用户ID") String userId,
        @P("返回最近几条记录，默认10条") Integer limit
    ) {
        try {
            String conversationId = "user_" + userId;
            List<String> history = conversationHistory.get(conversationId);
            
            if (history == null || history.isEmpty()) {
                return "📝 暂无对话历史记录";
            }
            
            int actualLimit = limit != null ? limit : 10;
            int startIndex = Math.max(0, history.size() - actualLimit);
            
            StringBuilder result = new StringBuilder("📚 对话历史记录:\n\n");
            for (int i = startIndex; i < history.size(); i++) {
                result.append(history.get(i)).append("\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            log.error("获取对话历史失败", e);
            return "❌ 获取失败: " + e.getMessage();
        }
    }
    
    @Tool("清除对话历史")
    public String clearConversationHistory(@P("用户ID") String userId) {
        try {
            String conversationId = "user_" + userId;
            conversationHistory.remove(conversationId);
            
            log.info("清除对话历史成功，用户ID: {}", userId);
            return "✅ 对话历史已清除";
            
        } catch (Exception e) {
            log.error("清除对话历史失败", e);
            return "❌ 清除失败: " + e.getMessage();
        }
    }
    
    @Tool("获取对话统计")
    public String getConversationStats(@P("用户ID") String userId) {
        try {
            String conversationId = "user_" + userId;
            List<String> history = conversationHistory.get(conversationId);
            
            if (history == null || history.isEmpty()) {
                return "📊 对话统计: 暂无记录";
            }
            
            int totalMessages = history.size();
            int userMessages = (int) history.stream()
                .filter(msg -> msg.contains("- 用户:"))
                .count();
            int aiMessages = totalMessages - userMessages;
            
            return String.format("""
                📊 对话统计:
                - 总消息数: %d
                - 用户消息: %d
                - AI回复: %d
                - 最后活动: %s
                """, totalMessages, userMessages, aiMessages, 
                history.isEmpty() ? "无" : "刚刚");
                
        } catch (Exception e) {
            log.error("获取对话统计失败", e);
            return "❌ 统计失败: " + e.getMessage();
        }
    }
}
