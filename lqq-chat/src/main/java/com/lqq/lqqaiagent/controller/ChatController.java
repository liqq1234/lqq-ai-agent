package com.lqq.lqqaiagent.controller;

import com.lqq.lqqaiagent.common.BaseResponse;
import com.lqq.lqqaiagent.common.ResultUtils;
import com.lqq.lqqaiagent.exception.ErrorCode;
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
import org.springframework.web.bind.annotation.RequestMethod;
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
    public BaseResponse<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String conversationId = request.get("conversationId");
        log.info("收到聊天请求: {}, conversationId={}", message, conversationId);
        
        try {
            String response = chatModel.chat(message);
            log.info("聊天响应完成，长度: {}", response.length());
            
            Map<String, Object> data = Map.of(
                "message", response,
                "timestamp", System.currentTimeMillis()
            );
            return ResultUtils.success(data);
        } catch (Exception e) {
            log.error("聊天请求处理失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 流式聊天接口 - 使用 Server-Sent Events (SSE)
     * 支持 GET 和 POST 两种方式
     */
    @RequestMapping(value = "/stream", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestParam(value = "message", required = false) String messageParam,
            @RequestParam(value = "conversationId", required = false) Long conversationIdParam,
            @RequestBody(required = false) Map<String, String> request) {
        // 优先使用 GET 参数，否则使用 POST body
        String message = messageParam != null ? messageParam : 
                        (request != null ? request.get("message") : null);
        Long conversationId = conversationIdParam != null ? conversationIdParam : 
                        (request != null && request.get("conversationId") != null
                                ? Long.valueOf(request.get("conversationId"))
                                : null);
        log.info("收到流式聊天请求: {}, conversationId={}", message, conversationId);
        
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
    public BaseResponse<Map<String, Object>> health() {
        Map<String, Object> data = Map.of(
            "status", "ok",
            "timestamp", System.currentTimeMillis(),
            "chatModel", chatModel != null ? "available" : "unavailable",
            "streamingChatModel", streamingChatModel != null ? "available" : "unavailable"
        );
        return ResultUtils.success(data);
    }
}
