package com.lqq.lqqaiagent.constant;

/**
 * 应用相关常量
 */
public interface AppConstant {

    /**
     * 默认优先级
     */
    Integer DEFAULT_PRIORITY = 0;

    /**
     * 删除状态：未删除
     */
    Integer NOT_DELETED = 0;

    /**
     * 删除状态：已删除
     */
    Integer DELETED = 1;

    /**
     * 应用列表缓存 Key
     */
    String APP_LIST_CACHE_KEY = "app:list:hot";

    /**
     * 列表缓存过期时间（分钟）
     */
    Long LIST_CACHE_EXPIRE_TIME = 10L;
}
