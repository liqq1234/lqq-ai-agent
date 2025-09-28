package com.lqq.lqqaiagent.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * MySQL聊天记忆存储控制器
 * 提供基于MySQL数据库的聊天记忆功能，支持多用户会话管理
 *
 * @author LQQ
 * @since 2025-09-23
 */
@Slf4j
@RequestMapping("/memory")
@RestController
public class MySqlChatStoreController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public MySqlChatStoreController(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    /**
     * 编程助手专用接口
     * 集成编程相关的系统提示词，专门用于编程问答
     *
     * @param userId 用户ID
     * @param inputMsg 编程相关问题
     * @return AI的编程建议和代码示例
     */
    @GetMapping("/chat")
    public String codingChat(@RequestParam String userId, @RequestParam String inputMsg) {
        log.info("用户 {} 发送编程问题: {}", userId, inputMsg);

        String systemPrompt = "你是一个专业的编程助手，精通各种编程语言和开发框架。" +
                " 请用简洁明了的中文回答用户的问题，" +
                " 并给出可运行的示例代码或详细的解决方案，" +
                " 遇到错误和异常时提供排查建议，" +
                " 回答要条理清晰，尽量提供完整代码片段。";

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(inputMsg)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, userId))
                .call()
                .content();

        log.info("AI编程助手回复用户 {}: {}", userId, response);
        return response;
    }

    /**
     * 获取用户对话历史
     *
     * @param userId 用户ID
     * @return 该用户的对话历史记录
     */
    @GetMapping("/history")
    public String getChatHistory(@RequestParam String userId) {
        log.info("获取用户 {} 的对话历史", userId);

        // 这里可以直接从chatMemory获取历史记录
        // 或者调用你的AiChatMemoryService来获取
        return "用户 " + userId + " 的对话历史功能开发中...";
    }

}