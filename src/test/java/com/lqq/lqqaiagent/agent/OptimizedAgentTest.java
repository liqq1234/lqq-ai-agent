package com.lqq.lqqaiagent.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.lqq.lqqaiagent.agent.tools.SubmitTool;
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
 * 优化后的智能体测试
 * 
 * 验证智能体在简单任务上的执行效率
 * 确保不会出现20步执行简单问答的问题
 * 
 * @author LQQ
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class OptimizedAgentTest {

    @Resource
    private DashScopeChatModel dashScopeChatModel;

    private YuManus yuManus;
    private List<Object> optimizedTools;

    @BeforeEach
    void setUp() {
        // 创建优化后的工具列表
        optimizedTools = List.of(
                new SubmitTool(),    // 提交答案工具 - 优先
                new TerminateTool()  // 终止工具 - 备用
        );
        
        // 创建优化后的 YuManus 实例
        yuManus = new YuManus(optimizedTools, dashScopeChatModel);
        
        log.info("优化测试准备完成，可用工具: {}", yuManus.getToolCount());
    }

    /**
     * 测试简单自我介绍的执行效率
     * 
     * 期望：应该在1-3步内完成，而不是20步
     */
    @Test
    void testSimpleIntroductionEfficiency() {
        log.info("=== 测试简单自我介绍的执行效率 ===");
        
        String userInput = "你好，请简单介绍一下你自己";
        
        long startTime = System.currentTimeMillis();
        String result = yuManus.runWithDetails(userInput);
        long endTime = System.currentTimeMillis();
        
        // 验证执行结果
        assertNotNull(result);
        assertFalse(result.trim().isEmpty());
        
        // 验证执行效率 - 关键测试点
        int executedSteps = yuManus.getCurrentStepNumber();
        log.info("执行步数: {}", executedSteps);
        
        // 断言：简单自我介绍应该在5步以内完成
        assertTrue(executedSteps <= 5, 
                String.format("简单自我介绍执行了 %d 步，超出预期的5步以内", executedSteps));
        
        // 验证最终状态
        assertTrue(yuManus.isFinished(), "任务应该正确完成");
        
        // 验证执行时间合理（应该很快完成）
        long duration = endTime - startTime;
        assertTrue(duration < 30000, // 30秒内
                String.format("执行时间 %d ms 过长，应该在30秒内完成", duration));
        
        log.info("✅ 效率测试通过");
        log.info("- 执行步数: {} 步（目标: ≤5步）", executedSteps);
        log.info("- 执行时间: {} ms（目标: <30秒）", duration);
        log.info("- 最终状态: {}", yuManus.getState());
        log.info("- 执行结果长度: {} 字符", result.length());
    }

    /**
     * 测试简单问候的执行效率
     */
    @Test
    void testSimpleGreetingEfficiency() {
        log.info("=== 测试简单问候的执行效率 ===");
        
        String userInput = "你好";
        String result = yuManus.runWithDetails(userInput);
        
        // 验证执行效率
        int executedSteps = yuManus.getCurrentStepNumber();
        assertTrue(executedSteps <= 3, 
                String.format("简单问候执行了 %d 步，应该在3步以内", executedSteps));
        
        assertTrue(yuManus.isFinished(), "任务应该正确完成");
        
        log.info("✅ 简单问候效率测试通过 - 执行步数: {}", executedSteps);
    }

    /**
     * 测试能力询问的执行效率
     */
    @Test
    void testCapabilityInquiryEfficiency() {
        log.info("=== 测试能力询问的执行效率 ===");
        
        String userInput = "你有什么能力？";
        String result = yuManus.runWithDetails(userInput);
        
        // 验证执行效率
        int executedSteps = yuManus.getCurrentStepNumber();
        assertTrue(executedSteps <= 4, 
                String.format("能力询问执行了 %d 步，应该在4步以内", executedSteps));
        
        assertTrue(yuManus.isFinished(), "任务应该正确完成");
        
        log.info("✅ 能力询问效率测试通过 - 执行步数: {}", executedSteps);
    }

    /**
     * 测试工具可用性
     */
    @Test
    void testOptimizedToolAvailability() {
        log.info("=== 测试优化后的工具可用性 ===");
        
        // 验证新工具可用
        assertTrue(yuManus.hasTool("Submit"));
        assertTrue(yuManus.hasTool("Terminate"));
        
        // 验证工具数量
        assertEquals(2, yuManus.getToolCount());
        
        log.info("可用工具列表:\n{}", yuManus.listAvailableTools());
        log.info("✅ 优化工具可用性测试通过");
    }

    /**
     * 压力测试 - 连续执行多个简单任务
     */
    @Test
    void testMultipleSimpleTasksEfficiency() {
        log.info("=== 压力测试 - 多个简单任务效率 ===");
        
        String[] simpleQuestions = {
            "你好",
            "你是谁？",
            "你能做什么？",
            "谢谢",
            "再见"
        };
        
        int totalSteps = 0;
        
        for (int i = 0; i < simpleQuestions.length; i++) {
            String question = simpleQuestions[i];
            log.info("执行第 {} 个任务: {}", i + 1, question);
            
            // 重置智能体
            yuManus.reset();
            
            // 执行任务
            String result = yuManus.runWithDetails(question);
            int steps = yuManus.getCurrentStepNumber();
            totalSteps += steps;
            
            // 验证每个任务都高效完成
            assertTrue(steps <= 5, 
                    String.format("任务 '%s' 执行了 %d 步，超出5步限制", question, steps));
            assertTrue(yuManus.isFinished(), 
                    String.format("任务 '%s' 未正确完成", question));
            
            log.info("任务 {} 完成，步数: {}", i + 1, steps);
        }
        
        double averageSteps = (double) totalSteps / simpleQuestions.length;
        log.info("✅ 压力测试通过");
        log.info("- 总任务数: {}", simpleQuestions.length);
        log.info("- 总步数: {}", totalSteps);
        log.info("- 平均步数: {:.1f} 步/任务", averageSteps);
        
        // 断言平均步数合理
        assertTrue(averageSteps <= 3.0, 
                String.format("平均步数 %.1f 过高，应该在3步以内", averageSteps));
    }
}
