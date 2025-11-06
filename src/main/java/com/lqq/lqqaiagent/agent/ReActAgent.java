package com.lqq.lqqaiagent.agent;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct (Reasoning and Acting) 模式的智能体抽象类
 * 
 * 实现了思考-行动的循环模式，这是 OpenManus 的核心设计模式
 * ReAct 模式将智能体的执行过程分解为两个阶段：
 * 1. Think (思考): 分析当前状态，决定下一步行动
 * 2. Act (行动): 执行决定的行动
 * 
 * 这种模式使智能体能够：
 * - 进行推理和规划
 * - 根据环境反馈调整策略
 * - 实现更复杂的任务执行
 * 
 * @author LQQ
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    // ==================== 抽象方法 ====================

    /**
     * 思考阶段 - 处理当前状态并决定下一步行动
     * 
     * 在这个阶段，智能体会：
     * 1. 分析当前的上下文和状态
     * 2. 评估可用的选项和工具
     * 3. 制定执行计划
     * 4. 决定是否需要执行行动
     * 
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 行动阶段 - 执行决定的行动
     * 
     * 在这个阶段，智能体会：
     * 1. 执行在思考阶段决定的行动
     * 2. 调用相应的工具或服务
     * 3. 处理执行结果
     * 4. 更新内部状态
     * 
     * @return 行动执行结果
     */
    public abstract String act();

    // ==================== 核心实现 ====================

    /**
     * 执行单个步骤：思考和行动
     * 
     * 这是 ReAct 模式的核心实现：
     * 1. 首先进行思考，决定是否需要行动
     * 2. 如果需要行动，则执行行动
     * 3. 如果不需要行动，则表示任务可能已完成
     * 
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            log.debug("智能体 [{}] 开始思考阶段", getName());
            
            // 1. 思考阶段
            boolean shouldAct = think();
            
            if (!shouldAct) {
                log.info("智能体 [{}] 思考完成，无需执行行动", getName());
                return "思考完成 - 无需行动";
            }
            
            log.debug("智能体 [{}] 开始行动阶段", getName());
            
            // 2. 行动阶段
            String actionResult = act();
            
            log.debug("智能体 [{}] 行动完成，结果: {}", getName(), actionResult);
            return actionResult;
            
        } catch (Exception e) {
            // 记录异常日志
            log.error("智能体 [{}] 在 ReAct 步骤执行中发生异常", getName(), e);
            return "步骤执行失败: " + e.getMessage();
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 记录思考过程
     * 
     * 子类可以调用此方法来记录思考的详细过程
     * 
     * @param thought 思考内容
     */
    protected void logThought(String thought) {
        log.info("智能体 [{}] 思考: {}", getName(), thought);
    }

    /**
     * 记录行动过程
     * 
     * 子类可以调用此方法来记录行动的详细过程
     * 
     * @param action 行动内容
     */
    protected void logAction(String action) {
        log.info("智能体 [{}] 行动: {}", getName(), action);
    }

    /**
     * 记录观察结果
     * 
     * 子类可以调用此方法来记录对环境的观察
     * 
     * @param observation 观察内容
     */
    protected void logObservation(String observation) {
        log.info("智能体 [{}] 观察: {}", getName(), observation);
    }

    // ==================== 模板方法 ====================

    /**
     * 思考前的准备工作
     * 
     * 子类可以重写此方法来进行思考前的准备
     */
    protected void beforeThink() {
        // 默认实现为空，子类可以重写
    }

    /**
     * 思考后的处理工作
     * 
     * 子类可以重写此方法来进行思考后的处理
     * 
     * @param shouldAct 是否应该执行行动
     */
    protected void afterThink(boolean shouldAct) {
        // 默认实现为空，子类可以重写
    }

    /**
     * 行动前的准备工作
     * 
     * 子类可以重写此方法来进行行动前的准备
     */
    protected void beforeAct() {
        // 默认实现为空，子类可以重写
    }

    /**
     * 行动后的处理工作
     * 
     * 子类可以重写此方法来进行行动后的处理
     * 
     * @param actionResult 行动结果
     */
    protected void afterAct(String actionResult) {
        // 默认实现为空，子类可以重写
    }

    // ==================== 增强的步骤执行 ====================

    /**
     * 增强版的步骤执行，包含完整的生命周期回调
     * 
     * @return 步骤执行结果
     */
    public String stepWithLifecycle() {
        try {
            // 思考前准备
            beforeThink();
            
            // 思考阶段
            boolean shouldAct = think();
            
            // 思考后处理
            afterThink(shouldAct);
            
            if (!shouldAct) {
                return "思考完成 - 无需行动";
            }
            
            // 行动前准备
            beforeAct();
            
            // 行动阶段
            String actionResult = act();
            
            // 行动后处理
            afterAct(actionResult);
            
            return actionResult;
            
        } catch (Exception e) {
            log.error("智能体 [{}] 在增强步骤执行中发生异常", getName(), e);
            return "步骤执行失败: " + e.getMessage();
        }
    }
}
