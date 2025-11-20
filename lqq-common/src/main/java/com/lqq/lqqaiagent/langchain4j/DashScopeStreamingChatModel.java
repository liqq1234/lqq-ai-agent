package com.lqq.lqqaiagent.langchain4j;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 自定义DashScope流式聊天语言模型实现
 * 
 * @author lqq
 * @date 2025-11-20
 */
@Slf4j
public class DashScopeStreamingChatModel implements StreamingChatModel {
    
    private final String apiKey;
    private final String modelName;
    private final Generation generation;
    
    public DashScopeStreamingChatModel(String apiKey) {
        this.apiKey = apiKey;
        this.modelName = "qwen-plus"; // 默认使用qwen-plus模型
        this.generation = new Generation();
        
        // 调试信息：检查API Key
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("❌ Streaming API Key为空或null");
        } else if (apiKey.startsWith("sk-")) {
            log.info("✅ Streaming API Key格式正确，长度: {}", apiKey.length());
        } else {
            log.warn("⚠️ Streaming API Key格式可能不正确: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
        }
    }
    
    @Override
    public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
        List<ChatMessage> messages = chatRequest.messages();
        
        CompletableFuture.runAsync(() -> {
            try {
                // 转换LangChain4j消息格式到DashScope格式
                List<Message> dashScopeMessages = convertMessages(messages);
                
                // 构建生成参数 - 启用流式输出
                GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(modelName)
                    .messages(dashScopeMessages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .topP(0.8)
                    .incrementalOutput(true) // 启用增量输出（流式）
                    .build();
                
                // 调用DashScope API - 注意：这里暂时使用同步调用模拟流式输出
                GenerationResult result = generation.call(param);
                
                // 处理响应
                if (result != null && result.getOutput() != null && result.getOutput().getChoices() != null) {
                    String content = result.getOutput().getChoices().get(0).getMessage().getContent();
                    
                    if (content != null && !content.isEmpty()) {
                        // 模拟流式输出：将内容分块发送
                        String[] words = content.split(" ");
                        StringBuilder currentChunk = new StringBuilder();
                        
                        for (int i = 0; i < words.length; i++) {
                            currentChunk.append(words[i]);
                            if (i < words.length - 1) {
                                currentChunk.append(" ");
                            }
                            
                            // 每3个词发送一次，或者是最后一个词
                            if ((i + 1) % 3 == 0 || i == words.length - 1) {
                                handler.onPartialResponse(currentChunk.toString());
                                log.debug("流式输出片段: {}", currentChunk.toString());
                                
                                // 模拟延迟
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                                currentChunk = new StringBuilder();
                            }
                        }
                        
                        // 发送完整响应
                        AiMessage aiMessage = AiMessage.from(content);
                        ChatResponse chatResponse = ChatResponse.builder()
                            .aiMessage(aiMessage)
                            .build();
                        handler.onCompleteResponse(chatResponse);
                        log.info("DashScope流式输出完成，总长度: {}", content.length());
                    }
                } else {
                    log.error("DashScope API返回结果为空");
                    AiMessage errorMessage = AiMessage.from("抱歉，我现在无法处理您的请求。");
                    ChatResponse errorResponse = ChatResponse.builder()
                        .aiMessage(errorMessage)
                        .build();
                    handler.onCompleteResponse(errorResponse);
                }
                
            } catch (ApiException e) {
                log.error("DashScope API调用失败", e);
                handler.onError(e);
            } catch (NoApiKeyException e) {
                log.error("DashScope API Key未配置", e);
                handler.onError(e);
            } catch (InputRequiredException e) {
                log.error("DashScope输入参数错误", e);
                handler.onError(e);
            } catch (Exception e) {
                log.error("未知错误", e);
                handler.onError(e);
            }
        });
    }
    
    /**
     * 转换LangChain4j消息格式到DashScope格式
     */
    private List<Message> convertMessages(List<ChatMessage> messages) {
        List<Message> dashScopeMessages = new ArrayList<>();
        
        for (ChatMessage message : messages) {
            Message dashScopeMessage = null;
            
            if (message instanceof SystemMessage) {
                dashScopeMessage = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(((SystemMessage) message).text())
                    .build();
            } else if (message instanceof UserMessage) {
                dashScopeMessage = Message.builder()
                    .role(Role.USER.getValue())
                    .content(((UserMessage) message).singleText())
                    .build();
            } else if (message instanceof AiMessage) {
                dashScopeMessage = Message.builder()
                    .role(Role.ASSISTANT.getValue())
                    .content(((AiMessage) message).text())
                    .build();
            }
            
            if (dashScopeMessage != null) {
                dashScopeMessages.add(dashScopeMessage);
            }
        }
        
        return dashScopeMessages;
    }
}
