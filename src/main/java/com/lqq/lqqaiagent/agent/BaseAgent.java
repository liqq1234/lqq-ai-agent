package com.lqq.lqqaiagent.agent;

import com.lqq.lqqaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 抽象基础智能体类，用于管理智能体状态和执行流程
 * 
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能
 * 子类必须实现 step 方法来定义具体的执行逻辑
 * 
 * 参考 OpenManus 的 BaseAgent 设计，结合 Spring AI 框架特性
 * 
 * @author LQQ
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // ==================== 核心属性 ====================
    
    /**
     * 智能体名称
     */
    private String name;

    /**
     * 智能体描述
     */
    private String description;

    // ==================== 提示词配置 ====================
    
    /**
     * 系统提示词 - 定义智能体的角色和能力
     */
    private String systemPrompt;

    /**
     * 下一步提示词 - 引导智能体进行下一步思考
     */
    private String nextStepPrompt;

    // ==================== 状态管理 ====================
    
    /**
     * 当前执行状态
     */
    private AgentState state = AgentState.IDLE;

    // ==================== 执行控制 ====================
    
    /**
     * 最大执行步数，防止无限循环
     * 
     * 参考业界最佳实践：
     * - Google ADK: 必须实现终止机制防止无限循环
     * - IBM ReAct: 建立最大循环次数限制延迟和成本
     */
    protected int maxSteps = 10;  // 参考鱼皮项目，设置为10步

    /**
     * 当前执行步数
     */
    private final AtomicInteger currentStep = new AtomicInteger(0);

    /**
     * 重复阈值 - 检测死循环的阈值
     * 
     * 参考 Google ADK 建议：设计子代理评估条件并发出终止信号
     */
    private int duplicateThreshold = 2;
    
    /**
     * 置信度阈值 - 当回答置信度足够高时提前终止
     * 
     * 参考 IBM ReAct 建议：循环可以在满足特定条件时结束
     */
    private double confidenceThreshold = 0.8;

    // ==================== AI 组件 ====================
    
    /**
     * Spring AI ChatClient - 用于与大模型交互
     */
    private ChatClient chatClient;

    // ==================== 记忆管理 ====================
    
    /**
     * 消息列表 - 维护会话上下文
     * 需要手动维护消息历史，确保上下文连续性
     */
    private List<Message> messageList = new ArrayList<>();

    // ==================== 辅助方法 ====================
    
    /**
     * 早期终止检测
     * 
     * 参考网上搜索的最佳实践：
     * - IBM ReAct: 循环可以在满足特定条件时结束
     * - Google ADK: 子代理评估条件并发出终止信号
     * 
     * @param stepResult 步骤执行结果
     * @return 是否应该提前终止
     */
    protected boolean shouldTerminateEarly(String stepResult) {
        if (stepResult == null) {
            return false;
        }
        
        String lowerResult = stepResult.toLowerCase();
        
        // 1. 检测明确的完成信号
        if (lowerResult.contains("任务完成") || 
            lowerResult.contains("task completed") ||
            lowerResult.contains("finished") ||
            lowerResult.contains("done")) {
            log.debug("检测到明确完成信号");
            return true;
        }
        
        // 2. 检测简单问答完成（参考IBM建议的置信度阈值）
        if (isHighConfidenceAnswer(stepResult)) {
            log.debug("检测到高置信度答案，提前终止");
            return true;
        }
        
        return false;
    }
    
    /**
     * 检测是否是高置信度的答案
     * 
     * 参考 IBM ReAct 建议：当模型识别出超过置信度阈值的潜在最终答案时终止
     */
    private boolean isHighConfidenceAnswer(String answer) {
        if (answer == null || answer.length() < 10) {
            return false;
        }
        
        String lowerAnswer = answer.toLowerCase();
        
        // 自我介绍类高置信度答案
        if ((lowerAnswer.contains("我是") || lowerAnswer.contains("i am")) && 
            (lowerAnswer.contains("yumanus") || lowerAnswer.contains("助手"))) {
            return true;
        }
        
        // 问候回复类高置信度答案
        if (lowerAnswer.contains("您好") && answer.length() > 20 && answer.length() < 100) {
            return true;
        }
        
        return false;
    }

    // ==================== 核心方法 ====================

    /**
     * 运行智能体
     * 
     * 主要执行流程：
     * 1. 状态检查和初始化
     * 2. 添加用户消息到上下文
     * 3. 循环执行步骤直到完成或达到最大步数
     * 4. 异常处理和资源清理
     * 
     * @param userPrompt 用户输入的提示词
     * @return 执行结果汇总
     */
    public String run(String userPrompt) {
        // 1. 前置检查
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("无法从当前状态启动智能体: " + this.state);
        }
        if (StringUtils.isBlank(userPrompt)) {
            throw new RuntimeException("用户提示词不能为空");
        }

        log.info("智能体 [{}] 开始执行任务", name);
        
        // 2. 状态转换和初始化
        transitionToState(AgentState.RUNNING);
        messageList.add(new UserMessage(userPrompt));
        currentStep.set(0);
        
        // 3. 执行结果收集
        List<String> results = new ArrayList<>();
        
        try {
            // 4. 主执行循环
            while (currentStep.get() < maxSteps && state != AgentState.FINISHED) {
                int stepNumber = currentStep.incrementAndGet();
                log.info("执行第 {}/{} 步", stepNumber, maxSteps);
                
                // 检测死循环
                if (isStuck()) {
                    handleStuckState();
                }
                
                // 执行单步
                String stepResult = step();
                results.add(String.format("第 %d 步: %s", stepNumber, stepResult));
                
                // 早期终止检测（参考网上最佳实践）
                if (shouldTerminateEarly(stepResult)) {
                    log.info("智能体 [{}] 满足早期终止条件，提前结束", name);
                    transitionToState(AgentState.FINISHED);
                    break;
                }
                
                log.debug("第 {} 步执行结果: {}", stepNumber, stepResult);
            }
            
            // 5. 检查是否超出步骤限制
            if (currentStep.get() >= maxSteps && state != AgentState.FINISHED) {
                transitionToState(AgentState.FINISHED);
                String terminationMsg = String.format("已达到最大步数限制 (%d)，任务终止", maxSteps);
                results.add(terminationMsg);
                log.warn("智能体 [{}] {}", name, terminationMsg);
            }
            
            log.info("智能体 [{}] 任务执行完成，共执行 {} 步", name, currentStep.get());
            return String.join("\n", results);
            
        } catch (Exception e) {
            // 6. 异常处理 - 只有在非终态时才转换到错误状态
            if (state != AgentState.FINISHED && state != AgentState.ERROR) {
                transitionToState(AgentState.ERROR);
            }
            log.error("智能体 [{}] 执行过程中发生错误", name, e);
            return "执行错误: " + e.getMessage();
        } finally {
            // 7. 资源清理
            cleanup();
        }
    }

    /**
     * 执行单个步骤
     * 
     * 子类必须实现此方法来定义具体的执行逻辑
     * 
     * @return 步骤执行结果
     */
    public abstract String step();

    // ==================== 状态管理方法 ====================

    /**
     * 安全的状态转换
     * 
     * @param targetState 目标状态
     */
    protected void transitionToState(AgentState targetState) {
        if (!state.canTransitionTo(targetState)) {
            throw new IllegalStateException(
                String.format("无法从状态 %s 转换到 %s", state, targetState));
        }
        
        AgentState previousState = this.state;
        this.state = targetState;
        log.debug("智能体 [{}] 状态转换: {} -> {}", name, previousState, targetState);
    }

    // ==================== 死循环检测和处理 ====================

    /**
     * 检测是否陷入死循环
     * 
     * 通过检查最近的消息是否重复来判断
     * 
     * @return 是否陷入循环
     */
    protected boolean isStuck() {
        if (messageList.size() < 2) {
            return false;
        }
        
        // 获取最后一条助手消息
        Message lastMessage = null;
        for (int i = messageList.size() - 1; i >= 0; i--) {
            Message msg = messageList.get(i);
            if ("assistant".equals(msg.getMessageType().getValue())) {
                lastMessage = msg;
                break;
            }
        }
        
        if (lastMessage == null) {
            return false;
        }
        
        // 统计重复次数
        int duplicateCount = 0;
        for (int i = messageList.size() - 2; i >= 0; i--) {
            Message msg = messageList.get(i);
            if ("assistant".equals(msg.getMessageType().getValue()) && 
                msg.getText().equals(lastMessage.getText())) {
                duplicateCount++;
            }
        }
        
        boolean stuck = duplicateCount >= duplicateThreshold;
        if (stuck) {
            log.warn("智能体 [{}] 检测到死循环，重复次数: {}", name, duplicateCount);
        }
        
        return stuck;
    }

    /**
     * 处理死循环状态
     * 
     * 通过修改下一步提示词来引导智能体跳出循环
     */
    protected void handleStuckState() {
        String stuckPrompt = "检测到重复响应，请考虑使用不同的策略或工具来解决问题。";
        if (StringUtils.isNotBlank(nextStepPrompt)) {
            this.nextStepPrompt = stuckPrompt + "\n" + this.nextStepPrompt;
        } else {
            this.nextStepPrompt = stuckPrompt;
        }
        log.info("智能体 [{}] 已更新提示词以避免死循环", name);
    }

    // ==================== 资源管理 ====================

    /**
     * 清理资源
     * 
     * 子类可以重写此方法来清理特定资源
     */
    protected void cleanup() {
        // 只有在 RUNNING 状态时才重置到 IDLE
        // 保持 FINISHED 和 ERROR 等终态不变
        if (state == AgentState.RUNNING) {
            this.state = AgentState.IDLE;
        }
        
        // 清理提示词修改
        // 注意：不清理 messageList，保留会话历史
        
        log.debug("智能体 [{}] 资源清理完成，当前状态: {}", name, state);
    }

    // ==================== 工具方法 ====================

    /**
     * 获取当前步数
     * 
     * @return 当前执行步数
     */
    public int getCurrentStepNumber() {
        return currentStep.get();
    }

    /**
     * 检查是否正在运行
     * 
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return state == AgentState.RUNNING;
    }

    /**
     * 检查是否已完成
     * 
     * @return 是否已完成
     */
    public boolean isFinished() {
        return state == AgentState.FINISHED;
    }

    /**
     * 检查是否有错误
     * 
     * @return 是否有错误
     */
    public boolean hasError() {
        return state == AgentState.ERROR;
    }
}
