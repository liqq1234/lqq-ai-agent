package com.lqq.lqqaiagent.constant;

/**
 * AI 相关常量
 */
public interface AiConstant {

    /**
     * AI 代码生成缓存 Key 前缀
     */
    String AI_CODE_CACHE_KEY = "ai:code:";

    /**
     * AI 代码生成缓存过期时间（分钟）
     */
    Long AI_CODE_CACHE_EXPIRE_TIME = 60L;
}
