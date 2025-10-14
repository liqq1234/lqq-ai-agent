package com.lqq.lqqaiagent.service.impl;

import com.lqq.lqqaiagent.model.dto.HtmlCodeResult;
import com.lqq.lqqaiagent.model.dto.MultiFileCodeResult;
import com.lqq.lqqaiagent.model.entity.User;
import com.lqq.lqqaiagent.service.AiCodeGeneratorService;
import com.lqq.lqqaiagent.util.CodeGenPrompts;
import com.lqq.lqqaiagent.util.UserContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * AI 代码生成服务实现类
 */
@Service
public class AiCodeGeneratorServiceImpl implements AiCodeGeneratorService {

    private final ChatClient inMemoryChatClient; // 未登录用户使用的模型客户端
    private final ChatClient mySqlChatClient;    // 已登录用户使用的模型客户端

    public AiCodeGeneratorServiceImpl(
            @Qualifier("inMemoryChatClient") ChatClient inMemoryChatClient,
            @Qualifier("mySqlChatClient") ChatClient mySqlChatClient) {
        this.inMemoryChatClient = inMemoryChatClient;
        this.mySqlChatClient = mySqlChatClient;
    }

    /**
     * 选择 ChatClient（根据登录状态）
     */

    private ChatClient chooseClient() {
        User currentUser = UserContext.getUser();
        boolean loggedIn = currentUser != null;
        return loggedIn ? mySqlChatClient : inMemoryChatClient;
    }

    /**
     * 生成 HTML 模式代码
     */
    @Override
    public HtmlCodeResult generateHtmlCode(String userMessage) {
        ChatClient client = chooseClient(); // 可根据业务判断登录状态
        String rawOutput = client.prompt(CodeGenPrompts.HTML_PROMPT + "\n" + userMessage)
                .call()
                .content();

        // 封装成结果对象
        HtmlCodeResult result = new HtmlCodeResult();
        result.setHtmlCode(rawOutput);
        return result;
    }

    /**
     * 生成多文件模式的代码（HTML + CSS + JS）
     */
    @Override
    public MultiFileCodeResult generateMultiFileCode(String userMessage) {
        ChatClient client = chooseClient();

        // 创建输出转换器，用于将模型输出解析成 Java Bean
        BeanOutputConverter<MultiFileCodeResult> outputConverter =
                new BeanOutputConverter<>(MultiFileCodeResult.class);
        String format = outputConverter.getFormat();

        // 构造提示词模板
        String promptText = CodeGenPrompts.MULTI_FILE_PROMPT;

        // 使用 PromptTemplate 渲染
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(promptText)
                .variables(Map.of(
                        "format", format,
                        "userMessage", userMessage
                ))
                .build();

        String renderedPrompt = promptTemplate.render();

        // 调用模型
        String rawOutput = client.prompt()
                .user(renderedPrompt)
                .call() // 在这里设置超时时间
                .content();

        // 转换成结构化对象
        return outputConverter.convert(rawOutput);
    }
}
