package com.lqq.lqqaiagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lqq.lqqaiagent.domain.User;
import com.lqq.lqqaiagent.domain.request.UserLoginRequest;
import com.lqq.lqqaiagent.domain.request.UserRegisterRequest;
import com.lqq.lqqaiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户模块 Controller
 */
@RestController
@RequestMapping("/user")
public class UsersController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Long register(@RequestBody UserRegisterRequest userRegisterRequest) {
     if(userRegisterRequest== null) {
         return null;
     }
     String username = userRegisterRequest.getUsername();
     String email = userRegisterRequest.getEmail();
     String password = userRegisterRequest.getPassword();
     String checkPassword = userRegisterRequest.getCheckPassword();
     if(StringUtils.isAnyBlank(username, password, checkPassword)) {
         return null;
     }
     return  userService.userRegister( email, username,password, checkPassword);
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
        return userService.uerLogin(email, password, request);
    }

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/search")
    public List<User> queryByEmail(@RequestParam String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)){
            queryWrapper.like("username", username);
        }
        return userService.list(queryWrapper);
    }
    /**
     * 根据用户id删除用户
     * @param id 用户id
     * @return 用户信息
     */
    @GetMapping("/search")
    public boolean queryByEmail(@RequestBody long id ) {
        if(id<=0) {
            return false;
        }
        return userService.removeById(id);
    }
}
