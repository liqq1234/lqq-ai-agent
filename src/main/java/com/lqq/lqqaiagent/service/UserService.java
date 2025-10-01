package com.lqq.lqqaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqq.lqqaiagent.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
* @author LQQ
* @description 针对表【users(用户表)】的数据库操作Service
* @createDate 2025-09-26 22:25:36
*/
@Service
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param email
     * @param password
     * @param checkPassword
     * @return
     */
    long userRegister(String email ,String username,String password,String checkPassword);
    /**
     * 用户登录
     * @param email
     * @param password
     * @return
     */
    User uerLogin(String email ,String password, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param user
     * @return
     */
    User toSafetyUser(User user);
}
