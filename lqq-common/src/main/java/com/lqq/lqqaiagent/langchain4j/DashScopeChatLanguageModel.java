package com.lqq.lqqaiagent.langchain4j;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.tools.FunctionDefinition;
import com.alibaba.dashscope.tools.ToolBase;
import com.alibaba.dashscope.tools.ToolCallBase;
import com.alibaba.dashscope.tools.ToolCallFunction;
import com.alibaba.dashscope.tools.ToolFunction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义DashScope聊天语言模型实现
 * 
 * @author lqq
 * @date 2025-11-08
 */
@Slf4j
public class DashScopeChatLanguageModel implements ChatModel {
    
    private final String apiKey;
    private final String modelName;
    private final Generation generation;
    private final ObjectMapper objectMapper;
    private final Gson gson;
    
    public DashScopeChatLanguageModel(String apiKey) {
        this.apiKey = apiKey;
        this.modelName = "qwen-plus"; // 默认使用qwen-plus模型
        this.generation = new Generation();
        this.objectMapper = new ObjectMapper();
        this.gson = new Gson();
        
        // 调试信息：检查API Key
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("❌ API Key为空或null");
        } else if (apiKey.startsWith("sk-")) {
            log.info("✅ API Key格式正确，长度: {}", apiKey.length());
        } else {
            log.warn("⚠️ API Key格式可能不正确: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
        }
    }
    
    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        List<ChatMessage> messages = chatRequest.messages();
        try {
            // 转换LangChain4j消息格式到DashScope格式
            List<Message> dashScopeMessages = convertMessages(messages);
            
            // 构建生成参数
            GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model(modelName)
                .messages(dashScopeMessages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .build();
            
            // 调用DashScope API
            GenerationResult result = generation.call(param);
            
            if (result != null && result.getOutput() != null && result.getOutput().getChoices() != null) {
                String content = result.getOutput().getChoices().get(0).getMessage().getContent();
                AiMessage aiMessage = AiMessage.from(content);
                
                log.debug("DashScope API调用成功，返回内容长度: {}", content.length());
                return ChatResponse.builder()
                    .aiMessage(aiMessage)
                    .build();
            } else {
                log.error("DashScope API返回结果为空");
                return ChatResponse.builder()
                    .aiMessage(AiMessage.from("抱歉，我现在无法处理您的请求。"))
                    .build();
            }
            
        } catch (ApiException e) {
            log.error("DashScope API调用失败", e);
            return ChatResponse.builder()
                .aiMessage(AiMessage.from("API调用失败: " + e.getMessage()))
                .build();
        } catch (NoApiKeyException e) {
            log.error("DashScope API Key未配置", e);
            return ChatResponse.builder()
                .aiMessage(AiMessage.from("API Key配置错误"))
                .build();
        } catch (InputRequiredException e) {
            log.error("DashScope输入参数错误", e);
            return ChatResponse.builder()
                .aiMessage(AiMessage.from("输入参数错误: " + e.getMessage()))
                .build();
        } catch (Exception e) {
            log.error("未知错误", e);
            return ChatResponse.builder()
                .aiMessage(AiMessage.from("处理请求时发生错误: " + e.getMessage()))
                .build();
        }
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
    
    
    /**
     * 转换LangChain4j工具规范到DashScope工具定义
     */
    private List<ToolBase> convertToolSpecifications(List<ToolSpecification> toolSpecifications) {
        List<ToolBase> tools = new ArrayList<>();
        
        for (ToolSpecification spec : toolSpecifications) {
            try {
                // 构建参数schema
                Map<String, Object> properties = new HashMap<>();
                List<String> required = new ArrayList<>();
                
                if (spec.parameters() != null) {
                    JsonNode parametersNode = objectMapper.readTree(spec.parameters().toString());
                    
                    if (parametersNode.has("properties")) {
                        JsonNode propertiesNode = parametersNode.get("properties");
                        propertiesNode.fields().forEachRemaining(entry -> {
                            Map<String, Object> property = new HashMap<>();
                            JsonNode propNode = entry.getValue();
                            
                            if (propNode.has("type")) {
                                property.put("type", propNode.get("type").asText());
                            }
                            if (propNode.has("description")) {
                                property.put("description", propNode.get("description").asText());
                            }
                            
                            properties.put(entry.getKey(), property);
                        });
                    }
                    
                    if (parametersNode.has("required")) {
                        parametersNode.get("required").forEach(node -> required.add(node.asText()));
                    }
                }
                
                // 构建参数schema - 使用JsonObject
                JsonObject parametersJson = new JsonObject();
                parametersJson.addProperty("type", "object");
                
                // 转换properties为JsonObject
                JsonObject propertiesJson = gson.toJsonTree(properties).getAsJsonObject();
                parametersJson.add("properties", propertiesJson);
                
                if (!required.isEmpty()) {
                    parametersJson.add("required", gson.toJsonTree(required));
                }
                
                // 创建函数定义
                FunctionDefinition functionDef = FunctionDefinition.builder()
                    .name(spec.name())
                    .description(spec.description())
                    .parameters(parametersJson)
                    .build();
                
                ToolFunction toolFunction = ToolFunction.builder()
                    .function(functionDef)
                    .build();
                
                tools.add((ToolBase) toolFunction);
                log.debug("转换工具规范成功: {}", spec.name());
                
            } catch (Exception e) {
                log.warn("转换工具规范失败: {} - {}", spec.name(), e.getMessage());
            }
        }
        
        return tools;
    }
}
