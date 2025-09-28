package com.lqq.lqqaiagent.service.impl;

import com.lqq.lqqaiagent.advisor.MyLoggerAdvisor;
import com.lqq.lqqaiagent.advisor.ReReadingAdvisor;
import com.lqq.lqqaiagent.service.ChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天服务实现类 - 业务逻辑层
 * 负责处理所有与AI聊天相关的业务逻辑
 *
 * @author LQQ
 * @since 2025-09-23
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    @Resource(name = "codingAppVectorStore")
    private VectorStore codingAppVectorStore;

    @Resource
    private Advisor codingAppRagCloudAdvisor;

    private static final String DEFAULT_SYSTEM_PROMPT =
            "你是一个专业的编程助手，精通各种编程语言和开发框架。"
                    + " 请用简洁明了的中文回答用户的问题，"
                    + " 并给出可运行的示例代码或详细的解决方案，"
                    + " 遇到错误和异常时提供排查建议，"
                    + " 回答要条理清晰，尽量提供完整代码片段。";

    /**
     * 构造函数，初始化ChatClient
     *
     * @param chatModel AI聊天模型（使用DashScope）
     * @param chatMemory 聊天记忆组件（MySQL实现）
     */
    public ChatServiceImpl(@Qualifier("dashscopeChatModel") ChatModel chatModel, ChatMemory chatMemory) {
        this.chatMemory = chatMemory;

        // 构建ChatClient，集成各种Advisor
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor(),
                        new ReReadingAdvisor()
                )
                .build();
    }

    @Override
    public String chatWithMemory(String message, String userId) {
        log.info("用户 {} 发送消息: {}", userId, message);

        try {
            String response = chatClient.prompt()
                    .user(message)
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, userId))
                    .call()
                    .content();

            log.info("AI回复用户 {}: {}", userId, response);
            return response;

        } catch (Exception e) {
            log.error("处理用户 {} 的消息时发生错误: {}", userId, e.getMessage(), e);
            return "抱歉，处理您的消息时遇到了问题，请稍后重试。";
        }
    }

    @Override
    public String simpleChat(String message) {
        log.info("收到简单聊天消息: {}", message);

        try {
            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();

            log.info("简单聊天回复: {}", response);
            return response;

        } catch (Exception e) {
            log.error("处理简单聊天消息时发生错误: {}", e.getMessage(), e);
            return "抱歉，处理您的消息时遇到了问题，请稍后重试。";
        }
    }

    @Override
    public CodingReport generateCodingReport(String message, String userId) {
        log.info("为用户 {} 生成编程报告，消息: {}", userId, message);

        try {
            String enhancedPrompt = DEFAULT_SYSTEM_PROMPT +
                    "每次对话后都要生成编程总结报告，标题为{用户名}的编程总结报告，内容为总结列表";

            CodingReport report = chatClient.prompt()
                    .system(enhancedPrompt)
                    .user(message)
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, userId))
                    .call()
                    .entity(CodingReport.class);

            log.info("生成编程报告: {}", report);
            return report;

        } catch (Exception e) {
            log.error("生成编程报告时发生错误: {}", e.getMessage(), e);
            return new CodingReport("错误报告", List.of("生成报告时遇到问题，请稍后重试"));
        }
    }

    @Override
    public int getChatHistoryCount(String userId) {
        try {
            // 这里可以通过ChatMemory获取历史记录信息
            // 具体实现取决于ChatMemory的接口
            return chatMemory.get(userId).size();
        } catch (Exception e) {
            log.error("获取用户 {} 聊天历史时发生错误: {}", userId, e.getMessage());
            return 0;
        }
    }

    @Override
    public void clearChatHistory(String userId) {
        try {
            chatMemory.clear(userId);
            log.info("已清除用户 {} 的聊天历史", userId);
        } catch (Exception e) {
            log.error("清除用户 {} 聊天历史时发生错误: {}", userId, e.getMessage());
        }
    }

    @Override
    public String chatWithRag(String message, String chatId) {
        log.info("用户 {} 发送RAG聊天消息: {}", chatId, message);

        try {
            ChatResponse chatResponse = chatClient
                    .prompt()
                    .user(message)
                    // 设置聊天记忆参数
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                    // 开启日志，便于观察效果
                    .advisors(new MyLoggerAdvisor())
                    // 应用知识库问答
                    .advisors(new QuestionAnswerAdvisor(codingAppVectorStore))
                    .call()
                    .chatResponse();

            String content = chatResponse.getResult().getOutput().getText();
            log.info("RAG聊天回复: {}", content);
            return content;

        } catch (Exception e) {
            log.error("处理RAG聊天消息时发生错误: {}", e.getMessage(), e);
            return "抱歉，处理您的消息时遇到了问题，请稍后重试。";
        }
    }
    @Override
    public String chatWithRagCloud(String message, String chatId) {

        log.info("用户 {} 发送RAG聊天消息: {}", chatId, message);

        try {
            ChatResponse chatResponse = chatClient
                    .prompt()
                    .user(message)
                    // 设置聊天记忆参数
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                    // 开启日志，便于观察效果
                    .advisors(new MyLoggerAdvisor())
                    // 应用知识库问答
                    .advisors(codingAppRagCloudAdvisor)
                    .call()
                    .chatResponse();

            String content = chatResponse.getResult().getOutput().getText();
            log.info("RAG聊天回复: {}", content);
            return content;

        } catch (Exception e) {
            log.error("处理RAG聊天消息时发生错误: {}", e.getMessage(), e);
            return "抱歉，处理您的消息时遇到了问题，请稍后重试。";
        }
    }
}
