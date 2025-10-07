package com.lqq.lqqaiagent.config;

import com.lqq.lqqaiagent.config.mysql.MySqlChatMemory;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Resource
    public ChatMemory mySqlChatMemory(MySqlChatMemory mySqlChatMemory) {
        // 已登录用户使用 MySQL 存储
        return mySqlChatMemory;
    }

    @Bean
    public ChatMemory inMemoryChatMemory() {
        InMemoryChatMemoryRepository repository = new InMemoryChatMemoryRepository();
        int MAX_MESSAGES = 100; // 内存中保存的最大消息数

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(MAX_MESSAGES)
                .build();
    }
}
