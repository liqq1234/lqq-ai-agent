package com.lqq.lqqaiagent.langchain4j;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j配置类
 * 使用标准OpenAI兼容模式连接DashScope
 * 
 * @author lqq
 * @date 2025-11-08
 */
@Slf4j
@Configuration
public class LangChain4jConfig {
    
    @Value("${langchain4j.open-ai.chat-model.base-url:http://localhost:11434/v1}")
    private String openAiBaseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key:ollama}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:qwen2.5:7b}")
    private String modelName;

    /**
     * 配置OpenAI兼容的DashScope聊天模型
     */
    @Bean
    public ChatModel chatModel() {
        log.info("初始化OpenAI兼容的聊天模型（默认指向本地 Ollama）");

        String baseUrl = openAiBaseUrl;
        String key = apiKey;

        log.info("Base URL: {}", baseUrl);
        log.info("Model Name: {}", modelName);
        log.info("API Key (prefix): {}", key == null ? "<null>" : key.substring(0, Math.min(10, key.length())) + "...");

        return OpenAiChatModel.builder()
            .baseUrl(baseUrl)
            .apiKey(key)
            .modelName(modelName)
            .maxTokens(8192)
            .logRequests(true)
            .logResponses(true)
            .build();
    }
    
    /**
     * 配置OpenAI兼容的流式聊天模型
     */
    @Bean
    public StreamingChatModel streamingChatModel() {
        log.info("初始化OpenAI兼容的流式聊天模型（默认指向本地 Ollama）");

        String baseUrl = openAiBaseUrl;
        String key = apiKey;

        log.info("Streaming Base URL: {}", baseUrl);
        log.info("Streaming Model Name: {}", modelName);

        return OpenAiStreamingChatModel.builder()
            .baseUrl(baseUrl)
            .apiKey(key)
            .modelName(modelName)
            .maxTokens(8192)
            .logRequests(true)
            .logResponses(true)
            .build();
    }
    
    /**
     * 配置HTML助手
     */
    @Bean
    public HtmlAssistant htmlAssistant(ChatModel chatModel, HtmlTool htmlTool) {
        log.info("初始化HTML助手");
        
        return AiServices.builder(HtmlAssistant.class)
            .chatModel(chatModel)
            .tools(htmlTool)
            .build();
    }
    
    /**
     * 配置通用助手
     */
    @Bean
    public UniversalAssistant universalAssistant(
        ChatModel chatModel, 
        HtmlTool htmlTool,
        FileOperationTool fileOperationTool,
        ChatMemoryTool chatMemoryTool
    ) {
        log.info("初始化通用助手");
        
        return AiServices.builder(UniversalAssistant.class)
            .chatModel(chatModel)
            .tools(htmlTool, fileOperationTool, chatMemoryTool)
            .build();
    }
}
