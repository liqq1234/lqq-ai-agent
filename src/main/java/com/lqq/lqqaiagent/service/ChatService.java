package com.lqq.lqqaiagent.service;

import java.util.List;

/**
 * 聊天服务接口 - 业务逻辑层
 * 定义所有与AI聊天相关的业务方法
 *
 * @author LQQ
 * @since 2025-09-23
 */
public interface ChatService {



    /**
     * 带记忆功能的聊天
     * 支持多用户会话，自动保存和加载对话历史
     *
     * @param message 用户消息
     * @param userId 用户ID，用于区分不同用户的会话
     * @return AI回复内容
     */
    String chatWithMemory(String message, String userId);

    /**
     * 简单聊天（无记忆功能）
     * 每次对话都是独立的，不保存历史记录
     *
     * @param message 用户消息
     * @return AI回复内容
     */
    String simpleChat(String message);

    /**
     * 编程报告功能（结构化输出）
     * 生成包含标题和建议列表的结构化报告
     *
     * @param message 用户消息
     * @param userId 用户ID
     * @return 编程报告对象
     */
    CodingReport generateCodingReport(String message, String userId);

    /**
     * 获取用户的聊天历史记录数量
     *
     * @param userId 用户ID
     * @return 历史记录数量
     */
    int getChatHistoryCount(String userId);

    /**
     * 清除用户的聊天历史
     *
     * @param userId 用户ID
     */
    void clearChatHistory(String userId);

    /**
     * 带RAG功能的聊天
     * 结合向量数据库进行知识检索增强的对话
     *
     * @param message 用户消息
     * @param chatId 会话ID
     * @return AI回复内容
     */
    String chatWithRag(String message, String chatId);
    /**
     * 带RAG云服务功能的聊天
     * 结合向量数据库进行知识检索增强的对话
     *
     * @param message 用户消息
     * @param chatId 会话ID
     * @return AI回复内容
     */
    String chatWithRagCloud(String message, String chatId);

    /**
     * 编程报告数据类
     */
    record CodingReport(String title, List<String> suggestions) {
    }
}
