package com.lqq.lqqaiagent.chat;

import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 简单测试：验证 ChatModel 是否能正常调用 DashScope 大模型
 */
@SpringBootTest
public class LangChain4jModelTest {

    @Autowired
    private ChatModel chatModel;

    @Test
    public void testChatModel() {
        String prompt = "用一句话自我介绍一下，你是从 lqq-ai-agent 这个项目里被调用的大模型";
        String response = chatModel.chat(prompt);
        System.out.println("模型返回结果:\n" + response);
    }
}
