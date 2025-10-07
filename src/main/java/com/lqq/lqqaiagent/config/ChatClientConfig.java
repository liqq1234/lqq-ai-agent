package com.lqq.lqqaiagent.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean("inMemoryChatClient")
    public ChatClient inMemoryChatClient(DashScopeChatModel model, ChatMemory inMemoryChatMemory) {
        return ChatClient.builder(model)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(inMemoryChatMemory)
                                .build()
                )
                .build();
    }

    @Bean("mySqlChatClient")
    public ChatClient mySqlChatClient(DashScopeChatModel model, ChatMemory mySqlChatMemory) {
        return ChatClient.builder(model)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(mySqlChatMemory)
                                .build()
                )
                .build();
    }
}
