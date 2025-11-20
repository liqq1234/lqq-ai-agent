package com.lqq.lqqaiagent.constant;

/**
 * 用户常量
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "userLoginState";

    // ------- 权限 --------
    /**
     * 默认权限
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员权限
     */
    String ADMIN_ROLE = "admin";

    // ------- Redis 缓存 --------
    /**
     * 用户缓存 Key 前缀
     */
    String USER_CACHE_KEY = "user:";

    /**
     * 用户缓存过期时间（分钟）
     */
    long CACHE_EXPIRE_TIME = 30;

    /**
     * 空对象缓存过期时间（分钟）- 防止缓存穿透
     */
    long NULL_CACHE_EXPIRE_TIME = 5;
}
