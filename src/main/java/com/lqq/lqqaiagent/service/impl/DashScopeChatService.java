package com.lqq.lqqaiagent.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.lqq.lqqaiagent.model.dto.CodeGenResult;
import com.lqq.lqqaiagent.util.CodeGenPrompts;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DashScopeChatService {

    private final ChatClient dashScopeChatClient;

    @Resource
    private ChatMemory mySqlChatMemory; // 已登录用户使用

    @Resource
    @Qualifier("inMemoryChatMemory") // 未登录用户使用
    private ChatMemory inMemoryChatMemory;


    public DashScopeChatService(@Qualifier("dashscopeChatModel") ChatModel chatModel) {
        this.dashScopeChatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(DashScopeChatOptions.builder()
                        .withTopP(0.7)
                        .build())
                .build();
    }

    /**
     * 通用生成逻辑
     */
    public String generate(String systemPrompt, String userInput) {
        return dashScopeChatClient.prompt(systemPrompt + "\n" + userInput)
                .call()
                .content();
    }


    /**
     * 使用 HTML 代码生成 prompt
     */
    public CodeGenResult generateHtml(String userInput) {
        String htmlContent = generate(CodeGenPrompts.HTML_PROMPT, userInput);
        return new CodeGenResult("html",htmlContent);
    }

    /**
     * 使用多文件项目生成 prompt
     */
    public CodeGenResult generateProject(String userInput) {
        String projectContent = generate(CodeGenPrompts.MULTI_FILE_PROMPT, userInput);
        return new CodeGenResult("project",projectContent);
    }
}
