package com.lqq.lqqaiagent.util;

import com.lqq.lqqaiagent.model.entity.User;

/**
 * 用户上下文工具类（基于 ThreadLocal 存储当前请求的登录用户）
 */
public class UserContext {

    // 每个线程独立存储一个 User 对象
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setUser(User user) {
        USER_HOLDER.set(user);
    }

    /**
     * 获取当前用户
     */
    public static User getUser() {
        return USER_HOLDER.get();
    }

    /**
     * 获取当前登录用户 ID（若未登录返回 null）
     */
    public static Long getCurrentUserId() {
        User user = USER_HOLDER.get();
        return user != null ? user.getId() : null;
    }

    /**
     * 清除当前线程的用户信息（防止内存泄漏）
     */
    public static void clear() {
        USER_HOLDER.remove();
    }
}
