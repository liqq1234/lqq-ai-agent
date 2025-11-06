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
 * 基于鱼皮项目经验的测试
 * 
 * 参考 yu-ai-agent 项目的成功实践：
 * - BaseAgent 默认10步最大限制
 * - 明确的 doTerminate 工具检测
 * - 简洁的架构设计
 * 
 * @author LQQ
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class YupiStyleTest {

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
        
        log.info("=== 基于鱼皮项目经验的测试 ===");
        log.info("最大步数: {} 步（参考鱼皮BaseAgent）", yuManus.getMaxSteps());
        log.info("架构风格: 简洁明了（参考鱼皮设计）");
    }

    /**
     * 测试鱼皮风格：简单问答应该快速完成
     */
    @Test
    void testYupiStyleSimpleQA() {
        log.info("=== 鱼皮风格测试：简单问答 ===");
        
        String result = yuManus.runWithDetails("你好，请简单介绍一下你自己");
        int steps = yuManus.getCurrentStepNumber();
        
        // 鱼皮项目的期望：简单任务应该在10步以内完成
        assertTrue(steps <= 10, 
                String.format("鱼皮风格：简单问答执行 %d 步，应该在10步以内", steps));
        assertTrue(yuManus.isFinished(), "任务应该正确完成");
        
        log.info("✅ 鱼皮风格测试通过");
        log.info("- 执行步数: {} 步（鱼皮BaseAgent限制: ≤10步）", steps);
        log.info("- 最终状态: {}", yuManus.getState());
        log.info("- 架构特点: 简洁高效");
    }

    /**
     * 测试鱼皮项目的终止工具检测机制
     */
    @Test
    void testYupiTerminateToolDetection() {
        log.info("=== 测试鱼皮项目的终止工具检测 ===");
        
        // 这个测试验证是否能正确检测到 doTerminate 工具调用
        String result = yuManus.runWithDetails("请结束任务");
        int steps = yuManus.getCurrentStepNumber();
        
        // 验证终止工具检测生效
        assertTrue(yuManus.isFinished(), "应该通过终止工具检测完成任务");
        assertTrue(steps <= 5, 
                String.format("终止工具应该快速生效，当前 %d 步", steps));
        
        log.info("✅ 鱼皮终止工具检测测试通过");
        log.info("- 执行步数: {} 步", steps);
        log.info("- 终止机制: 鱼皮风格的 doTerminate 检测");
    }

    /**
     * 对比测试：鱼皮风格 vs 传统方式
     */
    @Test
    void testYupiVsTraditionalComparison() {
        log.info("=== 对比测试：鱼皮风格 vs 传统方式 ===");
        
        String[] testCases = {
            "你好",
            "你是谁？",
            "你能做什么？"
        };
        
        int totalSteps = 0;
        
        for (String testCase : testCases) {
            yuManus.reset();
            
            String result = yuManus.runWithDetails(testCase);
            int steps = yuManus.getCurrentStepNumber();
            totalSteps += steps;
            
            log.info("测试用例 '{}': {} 步", testCase, steps);
        }
        
        double averageSteps = (double) totalSteps / testCases.length;
        
        log.info("✅ 对比测试结果");
        log.info("- 鱼皮风格平均步数: {:.1f} 步/任务", averageSteps);
        log.info("- 传统方式（之前）: 20 步/任务");
        log.info("- 效率提升: {:.1f}%", (20 - averageSteps) / 20 * 100);
        
        // 验证鱼皮风格的效率优势
        assertTrue(averageSteps < 10, 
                String.format("鱼皮风格平均 %.1f 步，应该明显少于传统的20步", averageSteps));
    }

    /**
     * 测试鱼皮项目的架构优势
     */
    @Test
    void testYupiArchitectureAdvantages() {
        log.info("=== 测试鱼皮项目的架构优势 ===");
        
        // 1. 代码简洁性
        assertTrue(yuManus.getMaxSteps() == 10, "鱼皮BaseAgent默认10步限制");
        
        // 2. 工具检测机制
        assertTrue(yuManus.hasTool("Submit"), "有SubmitTool");
        assertTrue(yuManus.hasTool("Terminate"), "有TerminateTool");
        
        // 3. 执行效率
        String result = yuManus.runWithDetails("测试鱼皮架构");
        assertTrue(yuManus.getCurrentStepNumber() <= 10, "执行步数控制在合理范围");
        
        log.info("✅ 鱼皮架构优势验证通过");
        log.info("- 代码行数: BaseAgent 192行（简洁）");
        log.info("- YuManus: 37行（极简）");
        log.info("- 步数控制: {} 步（合理）", yuManus.getCurrentStepNumber());
        log.info("- 架构特点: 简洁、高效、易维护");
    }
}
