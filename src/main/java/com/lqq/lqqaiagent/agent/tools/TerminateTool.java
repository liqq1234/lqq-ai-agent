package com.lqq.lqqaiagent.agent.tools;

import org.springframework.ai.tool.annotation.Tool;

import java.util.function.Function;

/**
 * 终止工具 - 智能体任务完成时调用
 * 
 * 当智能体完成所有任务或无法继续执行时，调用此工具来结束执行
 * 这是参考 OpenManus 设计的重要机制，防止智能体无限执行
 * 
 * @author LQQ
 */
public class TerminateTool implements Function<TerminateTool.Request, TerminateTool.Response> {

    /**
     * 终止请求参数
     */
    public record Request(
        String reason  // 终止原因
    ) {}

    /**
     * 终止响应结果
     */
    public record Response(
        String message,  // 终止消息
        boolean success  // 是否成功终止
    ) {}

    /**
     * 执行终止操作
     * 
     * @param request 终止请求
     * @return 终止结果
     */
    @Override
    public Response apply(Request request) {
        String reason = request.reason();
        if (reason == null || reason.trim().isEmpty()) {
            reason = "任务完成";
        }
        
        String message = String.format("智能体执行终止: %s", reason);
        return new Response(message, true);
    }

    /**
     * 终止智能体执行的工具方法
     * 
     * 这是一个带有 @Tool 注解的方法，Spring AI 会自动识别并注册为可调用的工具
     * 
     * @param reason 终止原因
     * @return 终止执行的响应消息
     */
    @Tool(description = """
            当任务完成或智能体无法继续执行时，调用此工具来终止执行。
            使用场景：
            1. 所有任务已完成
            2. 遇到无法解决的问题  
            3. 用户要求停止执行
            4. 达到执行目标
            """)
    public String doTerminate(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            reason = "任务完成";
        }
        
        String message = String.format("智能体执行终止: %s", reason);
        return message;
    }

    /**
     * 工具描述信息
     * 
     * @return 工具描述
     */
    public static String getDescription() {
        return """
                当任务完成或智能体无法继续执行时，调用此工具来终止执行。
                使用场景：
                1. 所有任务已完成
                2. 遇到无法解决的问题
                3. 用户要求停止执行
                4. 达到执行目标
                """;
    }
}
