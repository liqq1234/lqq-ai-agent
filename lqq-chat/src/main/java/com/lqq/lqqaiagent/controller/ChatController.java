package com.lqq.lqqaiagent.controller;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.data.message.AiMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;

/**
 * 聊天控制器 - 支持普通和流式聊天
 * 
 * @author lqq
 * @date 2025-11-20
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatModel chatModel;
    
    @Resource
    private StreamingChatModel streamingChatModel;

    /**
     * 普通聊天接口
     */
    @PostMapping("/message")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("收到聊天请求: {}", message);
        
        try {
            String response = chatModel.chat(message);
            log.info("聊天响应完成，长度: {}", response.length());
            
            return Map.of(
                "success", true,
                "message", response,
                "timestamp", System.currentTimeMillis()
            );
        } catch (Exception e) {
            log.error("聊天请求处理失败", e);
            return Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
        }
    }

    /**
     * 流式聊天接口 - 使用 Server-Sent Events (SSE)
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("收到流式聊天请求: {}", message);
        
        // 创建一个 Sink 来发送流式数据
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
        
        try {
            // 构建聊天请求
            ChatRequest chatRequest = ChatRequest.builder()
                .messages(UserMessage.from(message))
                .build();
            
            // 调用流式聊天模型
            streamingChatModel.chat(chatRequest, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    // 发送每个部分响应到前端（交给 Spring SSE 自动加 data: 前缀）
                    sink.tryEmitNext(partialResponse);
                    log.debug("发送流式片段: {}", partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse response) {
                    // 流式输出完成，发送结束标记
                    sink.tryEmitNext("[DONE]");
                    sink.tryEmitComplete();
                    log.info("流式聊天完成，总响应: {}", response.aiMessage().text());
                }

                @Override
                public void onError(Throwable error) {
                    // 处理错误（发送 JSON 文本作为数据）
                    String errorData = "{\"error\": \"" + error.getMessage() + "\"}";
                    sink.tryEmitNext(errorData);
                    sink.tryEmitComplete();
                    log.error("流式聊天出错", error);
                }
            });
            
        } catch (Exception e) {
            log.error("流式聊天请求处理失败", e);
            String errorData = "data: {\"error\": \"" + e.getMessage() + "\"}\n\n";
            sink.tryEmitNext(errorData);
            sink.tryEmitComplete();
        }
        
        return sink.asFlux();
    }
    
    /**
     * 简单的流式聊天接口 - 返回纯文本流
     */
    @PostMapping(value = "/stream-simple", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> streamChatSimple(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("收到简单流式聊天请求: {}", message);
        
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
        
        try {
            ChatRequest chatRequest = ChatRequest.builder()
                .messages(UserMessage.from(message))
                .build();
            
            streamingChatModel.chat(chatRequest, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    sink.tryEmitNext(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse response) {
                    sink.tryEmitComplete();
                    log.info("简单流式聊天完成");
                }

                @Override
                public void onError(Throwable error) {
                    sink.tryEmitError(error);
                    log.error("简单流式聊天出错", error);
                }
            });
            
        } catch (Exception e) {
            log.error("简单流式聊天请求处理失败", e);
            sink.tryEmitError(e);
        }
        
        return sink.asFlux();
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "ok",
            "timestamp", System.currentTimeMillis(),
            "chatModel", chatModel != null ? "available" : "unavailable",
            "streamingChatModel", streamingChatModel != null ? "available" : "unavailable"
        );
    }
}
