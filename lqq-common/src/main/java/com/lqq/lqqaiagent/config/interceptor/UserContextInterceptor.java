package com.lqq.lqqaiagent.config.interceptor;

import com.lqq.lqqaiagent.exception.ErrorCode;
import com.lqq.lqqaiagent.exception.ThrowUtils;
import com.lqq.lqqaiagent.model.entity.User;
import com.lqq.lqqaiagent.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.lqq.lqqaiagent.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 全局用户上下文拦截器
 * 每次请求前自动设置当前登录用户到 UserContext
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // 放行 OPTIONS 请求（CORS 预检请求）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从 Session 获取当前登录用户
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null, ErrorCode.NOT_LOGIN);
        if (userObj instanceof User user) {
            // 将当前登录用户放入 ThreadLocal
            UserContext.setUser(user);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // 清理上下文，防止内存泄漏
        UserContext.clear();
    }
}
