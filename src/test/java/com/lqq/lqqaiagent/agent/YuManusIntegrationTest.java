package com.lqq.lqqaiagent.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.lqq.lqqaiagent.agent.model.AgentState;
import com.lqq.lqqaiagent.agent.tools.TerminateTool;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * YuManus 智能体集成测试类
 * 
 * 这是一个完整的智能体测试套件，包含：
 * 1. 基础功能测试
 * 2. 工具调用测试
 * 3. 状态管理测试
 * 4. 异常处理测试
 * 5. 性能测试
 * 6. 集成测试
 * 
 * 测试策略：
 * - 单元测试：测试单个方法和功能
 * - 集成测试：测试智能体与外部系统的交互
 * - 端到端测试：测试完整的用户场景
 * 
 * @author LQQ
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class YuManusIntegrationTest {

    @Resource
    private DashScopeChatModel dashScopeChatModel;

    @Resource
    private List<Object> basicTools;

    private YuManus yuManus;

    @BeforeEach
    void setUp() {
        // 手动创建 YuManus 实例用于测试
        yuManus = new YuManus(basicTools, dashScopeChatModel);
        log.info("测试准备完成，智能体初始化成功");
    }

    /**
     * 测试智能体基本初始化
     */
    @Test
    void testAgentInitialization() {
        log.info("=== 测试智能体基本初始化 ===");
        
        // 验证基本属性
        assertNotNull(yuManus);
        assertEquals("YuManus", yuManus.getName());
        assertEquals(AgentState.IDLE, yuManus.getState());
        assertTrue(yuManus.getToolCount() > 0);
        assertNotNull(yuManus.getChatClient());
        
        // 打印初始化信息
        log.info("智能体名称: {}", yuManus.getName());
        log.info("初始状态: {}", yuManus.getState());
        log.info("可用工具数量: {}", yuManus.getToolCount());
        log.info(yuManus.listAvailableTools());
        
        log.info("智能体初始化测试通过 ✅");
    }

    /**
     * 测试简单对话功能
     */
    @Test
    void testSimpleConversation() {
        log.info("=== 测试简单对话功能 ===");
        
        String userInput = "你好，请简单介绍一下你自己";
        String result = yuManus.runWithDetails(userInput);
        
        // 验证执行结果
        assertNotNull(result);
        assertFalse(result.trim().isEmpty());
        
        // 验证状态变化
        assertTrue(yuManus.isFinished() || yuManus.hasError());
        
        log.info("对话结果:\n{}", result);
        log.info("最终状态: {}", yuManus.getState());
        log.info("简单对话测试通过 ✅");
    }

    /**
     * 测试任务完成检测
     */
    @Test
    void testTaskCompletionDetection() {
        log.info("=== 测试任务完成检测 ===");
        
        String userInput = "请说 'Hello World'，然后告诉我任务完成了";
        String result = yuManus.runWithDetails(userInput);
        
        // 验证执行结果
        assertNotNull(result);
        
        // 验证任务完成状态
        assertTrue(yuManus.isFinished());
        
        log.info("任务完成检测结果:\n{}", result);
        log.info("最终状态: {}", yuManus.getState());
        log.info("任务完成检测测试通过 ✅");
    }

    /**
     * 测试多步骤执行
     */
    @Test
    void testMultiStepExecution() {
        log.info("=== 测试多步骤执行 ===");
        
        String userInput = """
                请帮我完成以下任务：
                1. 首先介绍一下你的能力
                2. 然后解释一下 ReAct 模式
                3. 最后告诉我任务完成了
                """;
        
        String result = yuManus.runWithDetails(userInput);
        
        // 验证执行结果
        assertNotNull(result);
        assertTrue(yuManus.getCurrentStepNumber() > 1);
        
        log.info("多步骤执行结果:\n{}", result);
        log.info("执行步数: {}/{}", yuManus.getCurrentStepNumber(), yuManus.getMaxSteps());
        log.info("多步骤执行测试通过 ✅");
    }

    /**
     * 测试工具调用功能
     */
    @Test
    void testToolCalling() {
        log.info("=== 测试工具调用功能 ===");
        
        // 检查工具是否可用
        assertTrue(yuManus.hasTool("Terminate"));
        
        String userInput = "请使用终止工具结束任务，原因是测试完成";
        String result = yuManus.runWithDetails(userInput);
        
        // 验证执行结果
        assertNotNull(result);
        
        log.info("工具调用结果:\n{}", result);
        log.info("工具调用测试通过 ✅");
    }

    /**
     * 测试异常处理
     */
    @Test
    void testExceptionHandling() {
        log.info("=== 测试异常处理 ===");
        
        // 测试空输入
        assertThrows(RuntimeException.class, () -> yuManus.run(""));
        assertThrows(RuntimeException.class, () -> yuManus.run(null));
        
        // 测试无效状态
        yuManus.setState(AgentState.RUNNING);
        assertThrows(RuntimeException.class, () -> yuManus.run("test"));
        
        log.info("异常处理测试通过 ✅");
    }

    /**
     * 测试状态管理
     */
    @Test
    void testStateManagement() {
        log.info("=== 测试状态管理 ===");
        
        // 初始状态
        assertEquals(AgentState.IDLE, yuManus.getState());
        
        // 执行任务
        String result = yuManus.run("简单测试");
        
        // 验证状态变化
        assertNotEquals(AgentState.RUNNING, yuManus.getState());
        assertTrue(yuManus.isFinished() || yuManus.hasError());
        
        log.info("状态管理测试结果: {}", yuManus.getStatusInfo());
        log.info("状态管理测试通过 ✅");
    }

    /**
     * 测试智能体重置功能
     */
    @Test
    void testAgentReset() {
        log.info("=== 测试智能体重置功能 ===");
        
        // 执行一个任务
        yuManus.run("测试任务");
        
        // 记录重置前状态
        int messagesBefore = yuManus.getMessageList().size();
        log.info("重置前消息数量: {}", messagesBefore);
        
        // 重置智能体
        yuManus.reset();
        
        // 验证重置效果
        assertEquals(AgentState.IDLE, yuManus.getState());
        assertEquals(0, yuManus.getMessageList().size());
        
        log.info("重置后状态: {}", yuManus.getState());
        log.info("重置后消息数量: {}", yuManus.getMessageList().size());
        log.info("智能体重置测试通过 ✅");
    }

    /**
     * 性能测试
     */
    @Test
    void testPerformance() {
        log.info("=== 性能测试 ===");
        
        long startTime = System.currentTimeMillis();
        
        String userInput = "请简单回答：今天天气怎么样？";
        String result = yuManus.runWithDetails(userInput);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证结果
        assertNotNull(result);
        
        // 性能断言（根据实际情况调整）
        assertTrue(duration < 60000, "执行时间应该在60秒内，实际: " + duration + "ms");
        
        log.info("执行时间: {} ms", duration);
        log.info("响应长度: {} 字符", result.length());
        log.info("执行步数: {}", yuManus.getCurrentStepNumber());
        log.info("性能测试通过 ✅");
    }

    /**
     * 压力测试 - 连续执行多个任务
     */
    @Test
    void testStressExecution() {
        log.info("=== 压力测试 ===");
        
        String[] testPrompts = {
            "说一句问候语",
            "介绍一下你的能力",
            "解释什么是人工智能",
            "告诉我今天是星期几",
            "说再见"
        };
        
        for (int i = 0; i < testPrompts.length; i++) {
            log.info("执行第 {} 个测试任务", i + 1);
            
            // 重置智能体
            yuManus.reset();
            
            // 执行任务
            String result = yuManus.run(testPrompts[i]);
            
            // 验证结果
            assertNotNull(result);
            log.info("任务 {} 完成，结果长度: {}", i + 1, result.length());
        }
        
        log.info("压力测试通过 ✅");
    }
}
