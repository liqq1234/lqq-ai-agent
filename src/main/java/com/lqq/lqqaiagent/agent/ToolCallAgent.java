package com.lqq.lqqaiagent.agent;

import com.lqq.lqqaiagent.agent.model.AgentState;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

/**
 * 工具调用智能体 V2 - 简化版实现
 * 
 * 基于 Spring AI 的 ChatClient 内置工具调用功能，避免复杂的手动工具管理
 * 参考 OpenManus 的设计理念，结合 Spring AI 的最佳实践
 * 
 * 核心特性：
 * 1. 使用 ChatClient 的 tools() 方法自动处理工具调用
 * 2. 简化的 ReAct 模式实现
 * 3. 自动的工具选择和执行
 * 4. 智能的任务完成检测
 * 
 * @author LQQ
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // ==================== 核心属性 ====================

    /**
     * 可用的工具列表
     * 支持任何带有 @Tool 注解的对象或 Function 对象
     */
    private final List<Object> availableTools;

    /**
     * 当前执行的用户提示
     */
    private String currentUserPrompt;

    /**
     * 最后一次的响应结果
     */
    private String lastResponse;

    // ==================== 构造方法 ====================

    /**
     * 构造工具调用智能体
     * 
     * @param availableTools 可用的工具列表
     */
    public ToolCallAgent(List<Object> availableTools) {
        super();
        this.availableTools = availableTools != null ? availableTools : List.of();
    }

    // ==================== ReAct 实现 ====================

    /**
     * 思考阶段 - 准备下一步的执行
     * 在这个简化实现中，思考阶段主要是：
     * 1. 准备提示词
     * 2. 检查是否有可用工具
     * 3. 决定是否需要执行工具调用
     * 
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        try {
            // 1. 准备当前提示词
            if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
                currentUserPrompt = getNextStepPrompt();
                // 清空下一步提示，避免重复使用
                setNextStepPrompt(null);
            } else {
                currentUserPrompt = "请根据当前情况，选择合适的工具来完成任务。";
            }

            logThought("准备执行工具调用，提示词: " + currentUserPrompt);

            // 2. 检查是否有可用工具
            if (availableTools.isEmpty()) {
                log.warn("智能体 [{}] 没有可用工具", getName());
                transitionToState(AgentState.FINISHED);
                return false;
            }

            // 3. 检查是否已经完成任务
            if (lastResponse != null && isTaskCompleted(lastResponse)) {
                log.info("智能体 [{}] 检测到任务已完成", getName());
                transitionToState(AgentState.FINISHED);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("智能体 [{}] 思考过程中发生异常", getName(), e);
            return false;
        }
    }

    /**
     * 行动阶段 - 执行工具调用
     * 
     * 使用 Spring AI 的 ChatClient 内置工具调用功能：
     * 1. 调用 ChatClient 并传入所有可用工具
     * 2. 让 AI 自动选择和执行合适的工具
     * 3. 处理执行结果
     * 4. 更新状态和消息历史
     * 
     * @return 执行结果
     */
    @Override
    public String act() {
        try {
            // 1. 检查 ChatClient 是否可用
            if (getChatClient() == null) {
                return "ChatClient 未初始化，无法执行工具调用";
            }

            logAction("开始执行工具调用，可用工具数量: " + availableTools.size());

            // 2. 使用 ChatClient 进行工具调用
            // ChatClient 会自动处理工具选择、参数传递和执行
            String response = getChatClient()
                    .prompt()
                    .system(getSystemPrompt())
                    .user(currentUserPrompt)
                    .tools(availableTools.toArray())  // 传入所有可用工具
                    .call()
                    .content();

            // 3. 保存响应结果
            lastResponse = response;

            // 4. 更新消息历史
            if (currentUserPrompt != null) {
                getMessageList().add(new UserMessage(currentUserPrompt));
            }
            if (response != null && !response.trim().isEmpty()) {
                getMessageList().add(new AssistantMessage(response));
            }

            // 5. 检查任务完成状态（参考鱼皮项目的简洁方案）
            log.debug("检查任务完成状态，响应内容: {}", response);
            if (isTaskCompleted(response)) {
                log.info("智能体 [{}] 任务执行完成", getName());
                transitionToState(AgentState.FINISHED);
            } else {
                log.debug("任务未完成，继续执行");
            }
            
            //检查是否调用了终止工具
            if (response != null && response.contains("doTerminate")) {
                log.info("智能体 [{}] 调用了终止工具，任务结束", getName());
                transitionToState(AgentState.FINISHED);
            }

            logAction("工具调用完成，响应长度: " + (response != null ? response.length() : 0));
            return response != null ? response : "工具调用完成，但无响应内容";

        } catch (Exception e) {
            log.error("智能体 [{}] 工具调用过程中发生异常", getName(), e);
            return "工具调用失败: " + e.getMessage();
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 检查任务是否已完成
     * 参考 OpenManus 的最佳实践，实现多层次的任务完成检测
     * 
     * @param response AI 的响应内容
     * @return 是否已完成
     */
    private boolean isTaskCompleted(String response) {
        if (response == null) {
            return false;
        }
        
        // 1. 优先检测 SubmitTool 的明确完成信号
        if (response.contains("TASK_COMPLETED:")) {
            log.info("检测到明确的任务完成信号: SubmitTool");
            return true;
        }
        
        // 2. 检测传统的完成标识
        String lowerResponse = response.toLowerCase();
        boolean hasCompletionKeywords = lowerResponse.contains("任务完成") ||
               lowerResponse.contains("任务结束") ||
               lowerResponse.contains("执行完成") ||
               lowerResponse.contains("task completed") ||
               lowerResponse.contains("finished") ||
               lowerResponse.contains("done");
               
        if (hasCompletionKeywords) {
            log.info("检测到传统的任务完成标识");
            return true;
        }
        
        // 3. 检测简单问答的完成模式（参考 OpenManus 的智能检测）
        if (isSimpleQuestionAnswered(response)) {
            log.info("检测到简单问答已完成");
            return true;
        }
        
        return false;
    }
    
    /**
     * 检测是否是已完成的简单问答
     * 
     * 参考 OpenManus 的智能检测机制
     */
    private boolean isSimpleQuestionAnswered(String response) {
        if (response == null || response.length() < 10) {
            return false;
        }
        
        String lowerResponse = response.toLowerCase();
        
        // 检测自我介绍类回答
        if ((lowerResponse.contains("我是") || lowerResponse.contains("i am")) && 
            (lowerResponse.contains("yumanus") || lowerResponse.contains("助手") || lowerResponse.contains("ai"))) {
            return true;
        }
        
        // 检测能力介绍类回答
        if (lowerResponse.contains("可以帮助") || lowerResponse.contains("能够处理") || 
            lowerResponse.contains("can help") || lowerResponse.contains("able to")) {
            return true;
        }
        
        // 检测问候回复
        if ((lowerResponse.contains("您好") || lowerResponse.contains("hello")) && 
            response.length() > 20 && response.length() < 200) {
            return true;
        }
        
        return false;
    }

    // ==================== 工具管理方法 ====================

    /**
     * 获取可用工具列表
     * 
     * @return 工具列表
     */
    public List<Object> getAvailableTools() {
        return availableTools;
    }

    /**
     * 获取工具数量
     * 
     * @return 工具数量
     */
    public int getToolCount() {
        return availableTools != null ? availableTools.size() : 0;
    }

    /**
     * 检查是否有特定工具
     * 
     * @param toolName 工具名称或类名
     * @return 是否存在该工具
     */
    public boolean hasTool(String toolName) {
        if (availableTools == null || toolName == null) {
            return false;
        }
        
        for (Object tool : availableTools) {
            String className = tool.getClass().getSimpleName();
            if (className.toLowerCase().contains(toolName.toLowerCase()) ||
                toolName.toLowerCase().contains(className.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取最后一次的响应结果
     * 
     * @return 响应结果
     */
    public String getLastResponse() {
        return lastResponse;
    }

    /**
     * 列出所有可用工具的信息
     * 
     * @return 工具信息字符串
     */
    public String listAvailableTools() {
        if (availableTools.isEmpty()) {
            return "没有可用工具";
        }
        
        StringBuilder sb = new StringBuilder("可用工具列表:\n");
        for (int i = 0; i < availableTools.size(); i++) {
            Object tool = availableTools.get(i);
            sb.append(String.format("%d. %s\n", i + 1, tool.getClass().getSimpleName()));
        }
        return sb.toString();
    }

    // ==================== 资源清理 ====================

    /**
     * 清理资源
     * 
     * 重写父类方法，添加工具调用相关的清理逻辑
     */
    @Override
    protected void cleanup() {
        super.cleanup();
        
        // 清理工具调用相关数据
        this.currentUserPrompt = null;
        this.lastResponse = null;
        
        log.debug("智能体 [{}] 工具调用相关资源清理完成", getName());
    }
}
