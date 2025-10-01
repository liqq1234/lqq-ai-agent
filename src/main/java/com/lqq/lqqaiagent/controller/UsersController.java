package com.lqq.lqqaiagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lqq.lqqaiagent.common.BaseResponse;
import com.lqq.lqqaiagent.common.ErrorCode;
import com.lqq.lqqaiagent.common.ResultUtils;
import com.lqq.lqqaiagent.domain.User;
import com.lqq.lqqaiagent.domain.request.UserLoginRequest;
import com.lqq.lqqaiagent.domain.request.UserRegisterRequest;
import com.lqq.lqqaiagent.exception.BusinessException;
import com.lqq.lqqaiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.lqq.lqqaiagent.constant.UserConstant.ADMIN_ROLE;
import static com.lqq.lqqaiagent.constant.UserConstant.USER_LOGIN_STATE;

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
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest request) {
        try {
            Long userId = userService.userRegister(
                    request.getEmail(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getCheckPassword()
            );
            return ResultUtils.success(userId);
        }  catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<User> login(@RequestBody UserLoginRequest request, HttpServletRequest httpRequest) {
        try {
            User safetyUser = userService.userLogin(request.getEmail(), request.getPassword(), httpRequest);
            return ResultUtils.success(safetyUser);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> logout(HttpServletRequest request) {
        try {
            Integer result = userService.userLogout(request);
            return ResultUtils.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 根据用户名查询用户（脱敏）
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUser(@RequestParam String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH,"需要管理员权限");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }

        List<User> users = userService.list(queryWrapper);
        List<User> safetyUsers = users.stream()
                .map(userService::toSafetyUser)
                .collect(Collectors.toList());

        return ResultUtils.success(safetyUsers);
    }

    /**
     * 根据用户id删除用户
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestParam long id, HttpServletRequest request) {
        if (!isAdmin(request) || id <= 0) {
            throw new BusinessException(ErrorCode.NO_AUTH,"需要管理员权限");
        }

        boolean result = userService.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查询失败或数据不存在");
        }
        return ResultUtils.success(true);
    }

    /**
     * 判断当前登录用户是否是管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return loginUser != null && loginUser.getRole() == ADMIN_ROLE;
    }
}
