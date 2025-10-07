package com.lqq.lqqaiagent.service.impl;

import com.lqq.lqqaiagent.model.dto.CodeGenResult;
import com.lqq.lqqaiagent.util.CodeGenPrompts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;

@Service
public class DashScopeChatService {

    private final ChatClient inMemoryChatClient; // 未登录用户
    private final ChatClient mySqlChatClient;    // 已登录用户

    public DashScopeChatService(
            @Qualifier("inMemoryChatClient") ChatClient inMemoryChatClient,
            @Qualifier("mySqlChatClient") ChatClient mySqlChatClient) {
        this.inMemoryChatClient = inMemoryChatClient;
        this.mySqlChatClient = mySqlChatClient;
    }

    /**
     * 根据用户登录状态选择 ChatClient
     */
    public String generate(String systemPrompt, String userInput, boolean loggedIn) {
        ChatClient client = loggedIn ? mySqlChatClient : inMemoryChatClient;
        return client.prompt(systemPrompt + "\n" + userInput)
                .call()
                .content();
    }

    /**
     * HTML 代码生成
     */
    public CodeGenResult generateHtml(String userInput, boolean loggedIn) {
        String htmlContent = generate(CodeGenPrompts.HTML_PROMPT, userInput, loggedIn);
        return new CodeGenResult("html", htmlContent);
    }

    /**
     * 多文件项目生成
     */
    public CodeGenResult generateProject(String userInput, boolean loggedIn) {
        String projectContent = generate(CodeGenPrompts.MULTI_FILE_PROMPT, userInput, loggedIn);
        return new CodeGenResult("project", projectContent);
    }
}
