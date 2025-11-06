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
 * 快速验证优化效果的测试
 * 
 * @author LQQ
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class QuickTest {

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
        log.info("测试准备完成");
    }

    /**
     * 快速测试 - 验证简单问候是否能在3步内完成
     */
    @Test
    void quickTestSimpleGreeting() {
        log.info("=== 快速测试：简单问候 ===");
        
        String result = yuManus.runWithDetails("你好");
        int steps = yuManus.getCurrentStepNumber();
        
        log.info("执行步数: {}", steps);
        log.info("最终状态: {}", yuManus.getState());
        log.info("执行结果: {}", result);
        
        // 验证步数合理
        assertTrue(steps <= 5, String.format("步数 %d 过多，应该在5步以内", steps));
        assertTrue(yuManus.isFinished(), "任务应该完成");
        
        log.info("✅ 快速测试通过 - 步数: {}", steps);
    }
}
