package com.lqq.lqqaiagent.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import lombok.extern.slf4j.Slf4j;

/**
 * 提交答案工具 - 智能体完成任务时明确提交最终答案
 * 
 * 参考业界最佳实践，使用明确的submit工具来避免无限循环
 * 当智能体认为已经完成任务时，应该调用此工具提交最终答案
 * 
 * @author LQQ
 */
@Slf4j
public class SubmitTool {

    /**
     * 提交最终答案
     * 
     * 当智能体完成任务并准备好最终答案时调用此工具
     * 这是一个明确的信号，表示任务已完成，应该停止执行
     * 
     * @param answer 最终答案内容
     * @return 提交确认消息
     */
    @Tool(description = """
            当你已经完成用户的请求并准备好最终答案时，使用此工具提交答案。
            这会明确告诉系统任务已完成，避免继续不必要的步骤。
            
            使用场景：
            1. 回答了用户的问题
            2. 完成了用户要求的任务
            3. 提供了用户需要的信息
            4. 执行完所有必要的操作
            
            参数说明：
            - answer: 你要提交的最终答案或结果
            """)
    public String submitAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            answer = "任务已完成";
        }
        
        log.info("智能体提交最终答案: {}", answer);
        
        // 返回明确的完成标记
        String result = "任务完成: " + answer;
        log.info("SubmitTool 返回结果: {}", result);
        return result;
    }

    /**
     * 快速回答简单问题
     * 
     * 对于不需要使用其他工具的简单问答，可以直接提交答案
     * 
     * @param question 用户的问题
     * @param answer 直接的答案
     * @return 提交确认消息
     */
    @Tool(description = """
            对于简单的问答类任务，可以直接提供答案而无需使用其他工具。
            适用于自我介绍、基本信息查询等不需要外部工具的场景。
            
            使用场景：
            1. 自我介绍
            2. 说明自己的能力
            3. 简单的问候回复
            4. 基本信息说明
            
            参数说明：
            - question: 用户提出的问题
            - answer: 你的直接回答
            """)
    public String quickAnswer(String question, String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return "任务完成: 我无法回答这个问题。";
        }
        
        log.info("智能体快速回答问题 '{}': {}", question, answer);
        
        String result = "任务完成: " + answer;
        log.info("QuickAnswer 返回结果: {}", result);
        return result;
    }
}
