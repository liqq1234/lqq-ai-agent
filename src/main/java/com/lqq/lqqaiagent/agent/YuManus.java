package com.lqq.lqqaiagent.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * YuManus - 基于 Spring AI 的超级智能体
 * 
 * 这是一个全能型 AI 助手，能够解决用户提出的各种任务
 * 拥有多种工具调用能力，可以高效完成复杂请求
 * 
 * 核心特性：
 * 1. ReAct 模式执行 - 思考与行动循环
 * 2. 多工具集成 - 文件操作、网络搜索、PDF生成等
 * 3. 智能任务分解 - 自动将复杂任务分解为多个步骤
 * 4. 自主决策能力 - 根据任务需求选择合适的工具组合
 * 5. 状态管理 - 完整的执行状态跟踪和错误处理
 * 
 * 参考 OpenManus 的设计理念，结合 Spring AI 框架优势
 * 
 * @author LQQ
 */
@Component
@Slf4j
public class YuManus extends ToolCallAgent {

    // ==================== 系统提示词 ====================
    
    /**
     * 系统提示词 - 定义智能体的角色和能力
     */
    private static final String SYSTEM_PROMPT = """
            你是 YuManus，一个全能型 AI 助手，旨在高效解决用户提出的任务。
            
            重要原则 - 避免无限循环：
            1. 对于简单问答（如自我介绍、问候等），使用 quickAnswer 工具直接回答
            2. 完成任务后，立即使用 submitAnswer 工具提交最终答案
            3. 不要重复执行相同的操作或给出相同的回答
            
            你的核心能力包括：
            1. 文件操作 - 创建、读取、修改、删除文件和目录
            2. 网络搜索 - 搜索互联网获取最新信息
            3. 网页抓取 - 获取指定网页的详细内容
            4. 资源下载 - 下载图片、文档等网络资源
            5. 终端操作 - 执行系统命令和脚本
            6. PDF 生成 - 将内容转换为 PDF 文档
            
            工作流程：
            1. 分析用户需求的复杂度
            2. 简单问答 → 直接使用 quickAnswer 回答
            3. 复杂任务 → 分步执行，完成后使用 submitAnswer 提交结果
            4. 每完成一个任务就立即提交，不要等待或继续循环
            
            禁止行为：
            - 不要在没有新任务时继续执行步骤
            - 不要重复介绍自己或重复相同内容
            - 不要在完成任务后继续"思考"下一步
            """;

    /**
     * 下一步提示词 - 引导智能体高效完成任务
     */
    private static final String NEXT_STEP_PROMPT = """
            请高效完成用户的任务，避免不必要的循环：
            
            1. 如果是简单问答（自我介绍、问候、能力说明等）：
               → 立即使用 quickAnswer 工具直接回答
            
            2. 如果是复杂任务需要多步骤：
               → 执行必要步骤，完成后立即使用 submitAnswer 提交结果
            
            3. 如果任务已完成：
               → 不要继续思考，立即提交答案结束任务
            
            重要：每个任务只需要1-3步就应该完成，不要无限循环！
            """;

    // ==================== 构造方法 ====================

    /**
     * 构造 YuManus 智能体
     * 
     * @param basicTools 基本工具列表
     * @param dashscopeChatModel DashScope 聊天模型
     */
    public YuManus(List<Object> basicTools, DashScopeChatModel dashscopeChatModel) {
        super(basicTools);
        
        // 设置智能体基本信息
        this.setName("YuManus");
        this.setDescription("基于 Spring AI 的全能型超级智能体");
        
        // 设置提示词
        this.setSystemPrompt(SYSTEM_PROMPT);
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        
        // 设置执行参数
        this.setMaxSteps(20);  // 最大执行步数
        this.setDuplicateThreshold(2);  // 死循环检测阈值
        
        // 初始化 ChatClient
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .build();
        this.setChatClient(chatClient);
        
        log.info("YuManus 智能体初始化完成，可用工具数量: {}", getToolCount());
    }

    // ==================== 增强功能 ====================

    /**
     * 运行智能体并提供详细的执行信息
     * 
     * @param userPrompt 用户输入
     * @return 执行结果
     */
    public String runWithDetails(String userPrompt) {
        log.info("=== YuManus 智能体开始执行任务 ===");
        log.info("用户输入: {}", userPrompt);
        log.info("可用工具: {}", getToolCount());
        log.info("最大步数: {}", getMaxSteps());
        
        long startTime = System.currentTimeMillis();
        
        try {
            String result = run(userPrompt);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.info("=== YuManus 智能体任务执行完成 ===");
            log.info("执行时间: {} ms", duration);
            log.info("执行步数: {}/{}", getCurrentStepNumber(), getMaxSteps());
            log.info("最终状态: {}", getState());
            
            return result;
            
        } catch (Exception e) {
            log.error("YuManus 智能体执行失败", e);
            return "智能体执行失败: " + e.getMessage();
        }
    }

    /**
     * 获取智能体状态信息
     * 
     * @return 状态信息
     */
    public String getStatusInfo() {
        return String.format(
            "YuManus 状态信息:\n" +
            "- 当前状态: %s\n" +
            "- 执行步数: %d/%d\n" +
            "- 可用工具: %d\n" +
            "- 消息历史: %d 条\n" +
            "- 最后响应: %s",
            getState().getDescription(),
            getCurrentStepNumber(),
            getMaxSteps(),
            getToolCount(),
            getMessageList().size(),
            getLastResponse() != null ? "有" : "无"
        );
    }

    /**
     * 重置智能体状态
     * 
     * 清除消息历史，重置执行状态，准备新的任务
     */
    public void reset() {
        log.info("重置 YuManus 智能体状态");
        
        // 清理资源
        cleanup();
        
        // 清除消息历史
        getMessageList().clear();
        
        log.info("YuManus 智能体已重置，准备执行新任务");
    }

    // ==================== 工具管理 ====================

    /**
     * 列出所有可用工具
     * 
     * @return 工具名称列表
     */
    public String listAvailableTools() {
        return super.listAvailableTools();
    }

    /**
     * 检查特定工具是否可用
     * 
     * @param toolName 工具名称
     * @return 工具可用性信息
     */
    public String checkToolAvailability(String toolName) {
        boolean available = hasTool(toolName);
        return String.format("工具 '%s' %s", toolName, available ? "可用" : "不可用");
    }
}
