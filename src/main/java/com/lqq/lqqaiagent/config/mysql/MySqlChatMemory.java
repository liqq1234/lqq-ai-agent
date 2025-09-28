package com.lqq.lqqaiagent.config.mysql;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lqq.lqqaiagent.domain.AiChatMemory;
import com.lqq.lqqaiagent.mapper.AiChatMemoryMapper;
import com.lqq.lqqaiagent.service.AiChatMemoryService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于MySQL ChatMemory实现
 */
@Component
public class  MySqlChatMemory implements ChatMemory {

    @Resource
    private AiChatMemoryMapper aiChatMemoryMapper;
    @Resource
    private AiChatMemoryService aiChatMemoryService;


    @Override

    public void add(String conversationId, List<Message> messages) {
        List<AiChatMemory> aiChatMemorieList = new ArrayList<>();
        messages.forEach(message -> {
            AiChatMemory aiChatMemory = new AiChatMemory();
            aiChatMemory.setChatId(conversationId);
            aiChatMemory.setType(message.getMessageType().getValue());
            aiChatMemory.setContent(message.getText());
            aiChatMemorieList.add(aiChatMemory);
        });
        aiChatMemoryService.saveOrUpdateBatch(aiChatMemorieList);
    }

    @Override
    public List<Message> get(String conversationId) {
        int lastN = 20; // 默认获取最近20条消息
        List<AiChatMemory> aiChatMemoryList = aiChatMemoryMapper.selectList(
                new QueryWrapper<AiChatMemory>()
                        .eq("chat_id", conversationId)
                        .orderByAsc("create_time") // 改为正序，确保消息按时间顺序返回
                        .last("limit " + lastN)
        );

        if (CollectionUtils.isEmpty(aiChatMemoryList)) {
            return List.of();
        }

        return aiChatMemoryList.stream()
                .map(aiChatMemory -> {
                    String type = aiChatMemory.getType();
                    String content = aiChatMemory.getContent();
                    Message message; // 这里用接口类型
                    switch (type) {
                        case "system" -> message = new SystemMessage(content);
                        case "user" -> message = new UserMessage(content);
                        case "assistant" -> message = new AssistantMessage(content);
                        default -> throw new IllegalArgumentException("Unknown message type: " + type);
                    }
                    return message; // 返回接口类型
                })
                .toList();

    }

    @Override
    public void clear(String conversationId) {
        aiChatMemoryMapper.delete(new QueryWrapper<AiChatMemory>()
                .eq(conversationId!=null,"chat_id",conversationId));
    }
}