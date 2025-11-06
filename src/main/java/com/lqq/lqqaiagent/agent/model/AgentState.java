package com.lqq.lqqaiagent.agent.model;

/**
 * 智能体执行状态枚举类
 * 
 * 用于控制智能体的执行流程和状态转换
 * 参考 OpenManus 的状态管理机制
 * 
 * @author LQQ
 */
public enum AgentState {

    /**
     * 空闲状态 - 智能体未在执行任务
     */
    IDLE("idle", "空闲状态"),

    /**
     * 运行中状态 - 智能体正在执行任务
     */
    RUNNING("running", "运行中状态"),

    /**
     * 已完成状态 - 智能体已完成任务
     */
    FINISHED("finished", "已完成状态"),

    /**
     * 错误状态 - 智能体执行过程中发生错误
     */
    ERROR("error", "错误状态");

    /**
     * 状态代码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String description;

    AgentState(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取状态
     * 
     * @param code 状态代码
     * @return 对应的状态枚举
     */
    public static AgentState fromCode(String code) {
        for (AgentState state : values()) {
            if (state.code.equals(code)) {
                return state;
            }
        }
        throw new IllegalArgumentException("未知的状态代码: " + code);
    }

    /**
     * 检查是否可以从当前状态转换到目标状态
     * 
     * @param targetState 目标状态
     * @return 是否可以转换
     */
    public boolean canTransitionTo(AgentState targetState) {
        switch (this) {
            case IDLE:
                return targetState == RUNNING;
            case RUNNING:
                return targetState == FINISHED || targetState == ERROR;
            case FINISHED:
            case ERROR:
                return targetState == IDLE;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", description, code);
    }
}
