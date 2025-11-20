package com.lqq.lqqaiagent.langchain4j;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * HTML页面创建助手
 * 使用LangChain4j简化Agent实现
 * 
 * @author lqq
 * @date 2025-11-08
 */
public interface HtmlAssistant {
    
    @SystemMessage("""
        你是一个HTML页面创建专家。
        当用户要求创建HTML页面时，使用createHtmlFile工具。
        创建完成后直接返回结果，无需额外确认。
        
        你的任务：
        1. 理解用户需求
        2. 生成合适的HTML内容
        3. 调用工具创建文件
        4. 返回创建结果
        
        注意：一次对话完成任务，避免多轮确认。
        """)
    String createPage(@UserMessage String userRequest);
}
