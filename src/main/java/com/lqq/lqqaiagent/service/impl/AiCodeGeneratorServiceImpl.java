package com.lqq.lqqaiagent.service.impl;

import com.lqq.lqqaiagent.model.dto.HtmlCodeResult;
import com.lqq.lqqaiagent.model.dto.MultiFileCodeResult;
import com.lqq.lqqaiagent.model.entity.User;
import com.lqq.lqqaiagent.service.AiCodeGeneratorService;
import com.lqq.lqqaiagent.util.CodeGenPrompts;
import com.lqq.lqqaiagent.util.MD5Util;
import com.lqq.lqqaiagent.util.UserContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lqq.lqqaiagent.constant.AiConstant.*;

/**
 * AI 代码生成服务实现类
 */
@Slf4j
@Service
public class AiCodeGeneratorServiceImpl implements AiCodeGeneratorService {

    private final ChatClient inMemoryChatClient; // 未登录用户使用的模型客户端
    private final ChatClient mySqlChatClient;    // 已登录用户使用的模型客户端

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public AiCodeGeneratorServiceImpl(
            @Qualifier("inMemoryChatClient") ChatClient inMemoryChatClient,
            @Qualifier("mySqlChatClient") ChatClient mySqlChatClient) {
        this.inMemoryChatClient = inMemoryChatClient;
        this.mySqlChatClient = mySqlChatClient;
    }

    /**
     * 选择 ChatClient（根据登录状态）
     */

    private ChatClient chooseClient() {
        User currentUser = UserContext.getUser();
        boolean loggedIn = currentUser != null;
        return loggedIn ? mySqlChatClient : inMemoryChatClient;
    }

    /**
     * 生成 HTML 模式代码（带缓存）
     * 缓存策略：Cache Aside Pattern
     * 1. 先查缓存，命中则直接返回
     * 2. 缓存未命中，调用 AI 生成
     * 3. 将生成结果写入缓存
     *
     * @param userMessage 用户输入的描述
     * @return HTML 代码生成结果
     */
    @Override
    public HtmlCodeResult generateHtmlCode(String userMessage) {
        // 1. 生成缓存 Key（使用 MD5 避免特殊字符）
        String cacheKey = MD5Util.generateCacheKey(AI_CODE_CACHE_KEY, "html:" + userMessage);

        // 2. 从 Redis 查询缓存
        HtmlCodeResult cachedResult = (HtmlCodeResult) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            log.info("HTML 代码生成缓存命中，cacheKey={}", cacheKey);
            return cachedResult;
        }

        // 3. 缓存未命中，调用 AI 生成
        log.info("HTML 代码生成缓存未命中，调用 AI 模型");
        ChatClient client = chooseClient();
        String rawOutput = client.prompt(CodeGenPrompts.HTML_PROMPT + "\n" + userMessage)
                .call()
                .content();

        // 4. 封装成结果对象
        HtmlCodeResult result = new HtmlCodeResult();
        result.setHtmlCode(rawOutput);

        // 5. 写入缓存（60 分钟过期）
        redisTemplate.opsForValue().set(cacheKey, result, AI_CODE_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.info("HTML 代码生成结果已缓存，cacheKey={}", cacheKey);

        return result;
    }

    /**
     * 生成多文件模式的代码（HTML + CSS + JS）（带缓存）
     * 缓存策略：Cache Aside Pattern
     * 1. 先查缓存，命中则直接返回
     * 2. 缓存未命中，调用 AI 生成
     * 3. 将生成结果写入缓存
     *
     * @param userMessage 用户输入的描述
     * @return 多文件代码生成结果
     */
    @Override
    public MultiFileCodeResult generateMultiFileCode(String userMessage) {
        // 1. 生成缓存 Key（使用 MD5 避免特殊字符）
        String cacheKey = MD5Util.generateCacheKey(AI_CODE_CACHE_KEY, "multi:" + userMessage);

        // 2. 从 Redis 查询缓存
        MultiFileCodeResult cachedResult = (MultiFileCodeResult) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            log.info("多文件代码生成缓存命中，cacheKey={}", cacheKey);
            return cachedResult;
        }

        // 3. 缓存未命中，调用 AI 生成
        log.info("多文件代码生成缓存未命中，调用 AI 模型");
        ChatClient client = chooseClient();

        // 创建输出转换器，用于将模型输出解析成 Java Bean
        BeanOutputConverter<MultiFileCodeResult> outputConverter =
                new BeanOutputConverter<>(MultiFileCodeResult.class);
        String format = outputConverter.getFormat();

        // 构造提示词模板
        String promptText = CodeGenPrompts.MULTI_FILE_PROMPT;

        // 使用 PromptTemplate 渲染
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(promptText)
                .variables(Map.of(
                        "format", format,
                        "userMessage", userMessage
                ))
                .build();

        String renderedPrompt = promptTemplate.render();

        // 调用模型
        String rawOutput = client.prompt()
                .user(renderedPrompt)
                .call()
                .content();

        // 转换成结构化对象
        MultiFileCodeResult result = outputConverter.convert(rawOutput);

        // 4. 写入缓存（60 分钟过期）
        redisTemplate.opsForValue().set(cacheKey, result, AI_CODE_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.info("多文件代码生成结果已缓存，cacheKey={}", cacheKey);

        return result;
    }
}
