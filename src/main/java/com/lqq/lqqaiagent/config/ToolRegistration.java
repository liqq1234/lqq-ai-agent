package com.lqq.lqqaiagent.config;

import com.lqq.lqqaiagent.agent.tools.TerminateTool;
import com.lqq.lqqaiagent.agent.tools.SubmitTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 工具注册配置类
 * 
 * 简化版实现，只注册基本的工具：
 * 1. 终止工具 - 用于任务完成时终止执行
 * 
 * 注意：由于 Spring AI 的 FunctionCallback API 已弃用，
 * 这里使用简化的工具注册方式，主要工具通过其他方式集成
 * 
 * @author LQQ
 */
@Configuration
public class ToolRegistration {

    /**
     * 注册基本工具列表
     * 
     * @return 工具对象列表
     */
    @Bean
    public List<Object> basicTools() {
        return List.of(
                new SubmitTool(),    // 提交答案工具 - 优先使用
                new TerminateTool()  // 终止工具 - 备用
        );
    }

    /**
     * 获取工具描述信息
     * 
     * @return 工具描述映射
     */
    @Bean
    public String toolDescriptions() {
        return """
                可用工具说明：
                1. TerminateTool - 任务完成时调用，用于终止智能体执行
                
                使用方式：
                - 在 ChatClient 中通过 .tools() 方法传入工具对象
                - AI 会根据上下文自动选择合适的工具
                """;
    }
}
