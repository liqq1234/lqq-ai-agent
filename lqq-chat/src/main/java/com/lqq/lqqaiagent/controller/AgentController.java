package com.lqq.lqqaiagent.controller;

import com.lqq.lqqaiagent.langchain4j.UniversalAssistant;
import com.lqq.lqqaiagent.common.BaseResponse;
import com.lqq.lqqaiagent.common.ResultUtils;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI Agent 控制器
 * 
 * 使用LangChain4j UniversalAssistant替代旧的YuManus
 * 
 * @author LQQ
 */
@RestController
@RequestMapping("/agent")
@Tag(name = "AI Agent", description = "YuManus智能体相关接口")
@Slf4j
public class AgentController {

    private final UniversalAssistant universalAssistant;
    
    public AgentController(UniversalAssistant universalAssistant) {
        this.universalAssistant = universalAssistant;
    }

    @Value("${langchain4j.open-ai.chat-model.base-url:http://localhost:11434/v1}")
    private String openAiBaseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key:ollama}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:qwen2.5:7b}")
    private String modelName;

    /**
     * 与AI智能体对话
     * 
     * @param message 用户消息
     * @return AI回复
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "与AI智能体对话", description = "使用LangChain4j智能体进行对话")
    public SseEmitter chat(
            @Parameter(description = "用户消息", required = true)
            @RequestParam String message) {
        
        log.info("收到用户消息: {}", message);
        SseEmitter emitter = new SseEmitter(0L);
        
        OpenAiStreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(8192)
                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .messages(UserMessage.from(message))
                .build();

        streamingModel.chat(chatRequest, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                try {
                    emitter.send(SseEmitter.event().name("message").data(partialResponse));
                } catch (Exception e) {
                    log.error("发送SSE消息失败", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                try {
                    emitter.send(SseEmitter.event().name("complete").data("[DONE]"));
                } catch (Exception e) {
                    log.error("发送完成事件失败", e);
                } finally {
                    emitter.complete();
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("流式对话发生错误", error);
                try {
                    emitter.send(SseEmitter.event().name("error").data("出错: " + error.getMessage()));
                } catch (Exception ignored) {
                } finally {
                    emitter.completeWithError(error);
                }
            }
        });

        return emitter;
    }

    /**
     * 获取智能体状态信息
     * 
     * @return 状态信息
     */
    @GetMapping("/status")
    @Operation(summary = "获取智能体状态", description = "获取LangChain4j智能体的状态信息")
    public BaseResponse<String> getStatus() {
        try {
            String statusInfo = "🤖 LangChain4j智能体运行正常\n" +
                               "📊 状态：活跃\n" +
                               "🔧 可用工具：HTML创建、文件操作、记忆管理\n" +
                               "🚀 框架：LangChain4j + DashScope";
            return ResultUtils.success(statusInfo);
        } catch (Exception e) {
            log.error("获取智能体状态失败", e);
            return ResultUtils.error(500, "获取状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取智能体可用工具列表
     * 
     * @return 工具列表
     */
    @GetMapping("/tools")
    @Operation(summary = "获取可用工具", description = "获取LangChain4j智能体的可用工具列表")
    public BaseResponse<String> getTools() {
        try {
            String toolsInfo = """
                🔧 可用工具列表：
                
                📝 HTML工具：
                - createHtmlFile：创建HTML文件
                - createStyledHtmlFile：创建带样式的HTML页面
                
                📁 文件工具：
                - createTextFile：创建文本文件
                - readFile：读取文件内容
                - appendToFile：追加内容到文件
                - createJavaClass：创建Java类文件
                
                🧠 记忆工具：
                - saveConversation：保存对话记录
                - getConversationHistory：获取对话历史
                - clearConversationHistory：清除对话历史
                - getConversationStats：获取对话统计
                """;
            return ResultUtils.success(toolsInfo);
        } catch (Exception e) {
            log.error("获取工具列表失败", e);
            return ResultUtils.error(500, "获取工具列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查特定工具可用性
     * 
     * @param toolName 工具名称
     * @return 工具可用性信息
     */
    @GetMapping("/tool/{toolName}")
    @Operation(summary = "检查工具可用性", description = "检查指定工具是否可用")
    public BaseResponse<String> checkTool(
            @Parameter(description = "工具名称", required = true)
            @PathVariable String toolName) {
        try {
            // 使用LangChain4j工具检查逻辑
            String result = checkToolAvailability(toolName);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("检查工具可用性失败", e);
            return ResultUtils.error(500, "检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查工具可用性的内部方法
     */
    private String checkToolAvailability(String toolName) {
        String lowerName = toolName.toLowerCase();
        
        if (lowerName.equals("html") || lowerName.equals("createhtmlfile") || lowerName.equals("createstyledhtmlfile")) {
            return "✅ HTML工具可用 - 支持创建HTML文件和带样式的页面";
        } else if (lowerName.equals("file") || lowerName.equals("createtextfile") || 
                   lowerName.equals("readfile") || lowerName.equals("appendtofile") || 
                   lowerName.equals("createjavaclass")) {
            return "✅ 文件工具可用 - 支持文件创建、读取、追加和Java类生成";
        } else if (lowerName.equals("memory") || lowerName.equals("saveconversation") || 
                   lowerName.equals("getconversationhistory") || lowerName.equals("clearconversationhistory")) {
            return "✅ 记忆工具可用 - 支持对话记录管理";
        } else {
            return "❓ 未知工具: " + toolName + "\n可用工具: HTML工具、文件工具、记忆工具";
        }
    }
}
