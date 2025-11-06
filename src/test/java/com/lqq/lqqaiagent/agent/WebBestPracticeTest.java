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
 * 基于网上搜索的最佳实践测试
 * 
 * 参考资料：
 * - Google Agent Development Kit: 必须实现终止机制防止无限循环
 * - IBM ReAct Agent: 建立最大循环次数，在满足条件时终止
 * 
 * @author LQQ
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class WebBestPracticeTest {

    @Resource
    private DashScopeChatModel dashScopeChatModel;

    private YuManus yuManus;

    @BeforeEach
    void setUp() {
        List<Object> tools = List.of(
                new SubmitTool(),
                new TerminateTool()
        );
        yuManus = new YuManus(tools, dashScopeChatModel);
        
        log.info("=== 基于网上最佳实践的测试 ===");
        log.info("最大步数限制: {} 步（参考IBM建议）", yuManus.getMaxSteps());
        log.info("可用工具: {}", yuManus.getToolCount());
    }

    /**
     * 测试Google ADK建议：必须实现终止机制防止无限循环
     */
    @Test
    void testGoogleADKTerminationMechanism() {
        log.info("=== 测试 Google ADK 终止机制 ===");
        
        String result = yuManus.runWithDetails("你好，请简单介绍一下你自己");
        int steps = yuManus.getCurrentStepNumber();
        
        // 验证终止机制有效
        assertTrue(steps <= 5, 
                String.format("Google ADK建议：必须有终止机制。当前执行 %d 步，应该在5步以内", steps));
        assertTrue(yuManus.isFinished(), "应该正确终止");
        
        log.info("✅ Google ADK 终止机制测试通过");
        log.info("- 执行步数: {} 步", steps);
        log.info("- 最终状态: {}", yuManus.getState());
    }

    /**
     * 测试IBM ReAct建议：建立最大循环次数限制延迟和成本
     */
    @Test
    void testIBMReActMaxIterations() {
        log.info("=== 测试 IBM ReAct 最大迭代限制 ===");
        
        String result = yuManus.runWithDetails("你是谁？");
        int steps = yuManus.getCurrentStepNumber();
        
        // 验证最大迭代限制
        assertTrue(steps <= yuManus.getMaxSteps(), 
                String.format("IBM建议：建立最大循环次数。执行 %d 步，不应超过最大限制 %d", 
                        steps, yuManus.getMaxSteps()));
        
        log.info("✅ IBM ReAct 最大迭代限制测试通过");
        log.info("- 执行步数: {} 步", steps);
        log.info("- 最大限制: {} 步", yuManus.getMaxSteps());
    }

    /**
     * 测试IBM ReAct建议：循环在满足特定条件时结束
     */
    @Test
    void testIBMReActConditionalTermination() {
        log.info("=== 测试 IBM ReAct 条件终止 ===");
        
        String result = yuManus.runWithDetails("你有什么能力？");
        int steps = yuManus.getCurrentStepNumber();
        
        // 验证条件终止（高置信度答案）
        assertTrue(steps <= 3, 
                String.format("IBM建议：满足条件时终止。能力询问应该快速完成，当前 %d 步", steps));
        assertTrue(yuManus.isFinished(), "应该因满足条件而终止");
        
        log.info("✅ IBM ReAct 条件终止测试通过");
        log.info("- 执行步数: {} 步", steps);
        log.info("- 终止原因: 满足高置信度条件");
    }

    /**
     * 测试网上建议的综合效果
     */
    @Test
    void testOverallWebBestPractices() {
        log.info("=== 测试网上最佳实践综合效果 ===");
        
        String[] testCases = {
            "你好",           // 预期: 1-2步
            "你是谁？",       // 预期: 2-3步  
            "你能做什么？",   // 预期: 2-3步
            "谢谢"            // 预期: 1-2步
        };
        
        int totalSteps = 0;
        int maxAllowedSteps = 3; // 根据网上建议，简单任务应该很快完成
        
        for (int i = 0; i < testCases.length; i++) {
            String testCase = testCases[i];
            log.info("测试用例 {}: {}", i + 1, testCase);
            
            // 重置智能体
            yuManus.reset();
            
            // 执行测试
            String result = yuManus.runWithDetails(testCase);
            int steps = yuManus.getCurrentStepNumber();
            totalSteps += steps;
            
            // 验证每个用例都符合网上建议
            assertTrue(steps <= maxAllowedSteps, 
                    String.format("测试用例 '%s' 执行 %d 步，超出网上建议的 %d 步限制", 
                            testCase, steps, maxAllowedSteps));
            assertTrue(yuManus.isFinished(), 
                    String.format("测试用例 '%s' 未正确完成", testCase));
            
            log.info("用例 {} 完成: {} 步", i + 1, steps);
        }
        
        double averageSteps = (double) totalSteps / testCases.length;
        
        log.info("✅ 网上最佳实践综合测试通过");
        log.info("- 测试用例数: {}", testCases.length);
        log.info("- 总步数: {}", totalSteps);
        log.info("- 平均步数: {:.1f} 步/任务", averageSteps);
        log.info("- 网上建议: 简单任务应该快速完成");
        
        // 验证平均效果符合网上建议
        assertTrue(averageSteps <= 2.5, 
                String.format("平均步数 %.1f 过高，网上建议简单任务应该快速完成", averageSteps));
    }
}
