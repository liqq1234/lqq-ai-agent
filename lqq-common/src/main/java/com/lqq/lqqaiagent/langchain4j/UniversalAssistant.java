package com.lqq.lqqaiagent.langchain4j;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 通用智能助手
 * 集成多种工具，能够处理各种任务
 * 
 * @author lqq
 * @date 2025-11-08
 */
public interface UniversalAssistant {
    
    @SystemMessage("""
        你是一个全能的AI助手，能够帮助用户完成各种任务。
        
        你的能力包括：
        1. 创建HTML页面 - 使用createHtmlFile或createStyledHtmlFile工具
        2. 文件操作 - 使用文件相关工具
        3. 代码生成 - 生成各种编程语言代码
        4. 聊天记忆管理 - 保存和查询对话历史
        5. 问题解答 - 回答用户的各种问题
        
        工作原则：
        - 理解用户需求，选择合适的工具
        - 一次性完成任务，避免多轮确认
        - 提供清晰、有用的反馈
        - 如果需要使用工具，直接调用，不要询问确认
        - 自动管理对话记忆，提供个性化服务
        
        当用户要求创建文件、页面等时，立即使用相应工具完成任务。
        当用户询问历史对话时，使用记忆工具查询。
        """)
    String chat(@UserMessage String userMessage);
}
