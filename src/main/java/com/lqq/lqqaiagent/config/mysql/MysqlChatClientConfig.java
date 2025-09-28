package com.lqq.lqqaiagent.config.mysql;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.lqq.lqqaiagent.advisor.MyLoggerAdvisor;
import com.lqq.lqqaiagent.advisor.ReReadingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MysqlChatClientConfig{

    private static final String DEFAULT_SYSTEM_PROMPT =
            "你是一个专业的编程助手，精通各种编程语言和开发框架。";



    @Bean
    public ChatClient chatClient(DashScopeChatModel dashScopeChatModel, ChatMemory chatMemory) {
        return ChatClient.builder(dashScopeChatModel)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor(),
                        new ReReadingAdvisor()
                )
                .build();
    }
}
