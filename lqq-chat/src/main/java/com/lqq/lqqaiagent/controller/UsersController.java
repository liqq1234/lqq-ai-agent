package com.lqq.lqqaiagent.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqq.lqqaiagent.annotation.AuthCheck;
import com.lqq.lqqaiagent.common.BaseResponse;
import com.lqq.lqqaiagent.common.DeleteRequest;
import com.lqq.lqqaiagent.common.ResultUtils;
import com.lqq.lqqaiagent.constant.UserConstant;
import com.lqq.lqqaiagent.exception.ErrorCode;
import com.lqq.lqqaiagent.exception.ThrowUtils;
import com.lqq.lqqaiagent.model.dto.user.*;
import com.lqq.lqqaiagent.model.entity.User;
import com.lqq.lqqaiagent.model.vo.LoginUserVO;
import com.lqq.lqqaiagent.model.vo.UserVO;
import com.lqq.lqqaiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户模块 Controller（用户侧 + 部分管理员接口）
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UsersController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest request) {
        log.info("用户注册请求，邮箱：{}", request.getEmail());
        Long userId = userService.userRegister(
                request.getEmail(),
                request.getUserName(),
                request.getPassword(),
                request.getCheckPassword()
        );
        log.info("用户注册成功，userId：{}", userId);
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        log.info("用户登录请求，邮箱：{}", request.getEmail());
        LoginUserVO loginUserVO = userService.userLogin(request.getEmail(), request.getPassword(), httpRequest);
        log.info("用户登录成功，userId：{}", loginUserVO.getId());
        return ResultUtils.success(loginUserVO);

    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        log.info("用户注销请求");
        boolean result = userService.userLogout(request);
        log.info("用户注销成功");
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取用户包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);
        UserVO userVO = userService.getUserVO(user);
        return ResultUtils.success(userVO);
    }
}
