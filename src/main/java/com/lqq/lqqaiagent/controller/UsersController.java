package com.lqq.lqqaiagent.controller;

import com.lqq.lqqaiagent.domain.User;
import com.lqq.lqqaiagent.domain.request.UserLoginRequest;
import com.lqq.lqqaiagent.domain.request.UserRegisterRequest;
import com.lqq.lqqaiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户模块 Controller
 */
@RestController
@RequestMapping("/user")
public class UsersController {

    @Resource
    private UserService usersService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Long register(@RequestBody UserRegisterRequest userRegisterRequest) {
     if(userRegisterRequest== null) {
         return null;
     }
     String username = userRegisterRequest.getUsername();
     String password = userRegisterRequest.getPassword();
     String checkPassword = userRegisterRequest.getCheckPassword();
     if(StringUtils.isAnyBlank(username, password, checkPassword)) {
         return null;
     }
     return  usersService.userRegister(username, password, checkPassword);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public User login(@RequestBody UserLoginRequest userLoginRequest,HttpServletRequest request) {
        if(userLoginRequest== null) {
            return null;
        }
        String password = userLoginRequest.getPassword();
        String email= userLoginRequest.getEmail();
        if(StringUtils.isAnyBlank(email, password)) {
            return null;
        }
        return usersService.uerLogin(email, password, request);
    }
}
