package com.lqq.lqqaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqq.lqqaiagent.domain.User;
import com.lqq.lqqaiagent.exception.BusinessException;
import com.lqq.lqqaiagent.mapper.UserMapper;
import com.lqq.lqqaiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author LQQ
* @description 针对表【users(用户表)】的数据库操作Service实现
* @createDate 2025-09-26 22:25:36
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    @Resource
    private UserMapper userMapper;

    public static final  String USER_LOGIN_STATE = "userLoginState";

    @Override
    public long userRegister(String email, String password, String checkPassword) {

        // 1. 参数校验
        if (StringUtils.isAnyBlank(email, password, checkPassword)) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (!password.equals(checkPassword)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("密码长度不能少于8位");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        // 2. 检查邮箱是否已注册
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User existUser = userMapper.selectOne(queryWrapper);
        if (existUser != null) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 3. 密码加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encryptPassword = passwordEncoder.encode(password);

        // 4. 保存用户
        User user = new User();
        user.setEmail(email);
        user.setPassword(encryptPassword);
        userMapper.insert(user);

        // 5. 返回用户ID
        return user.getId();
    }

    @Override
    public User uerLogin(String email, String password, HttpServletRequest request) {
        // 1. 参数校验
        if (StringUtils.isBlank(email) || StringUtils.isBlank(password)) {
            throw new BusinessException(400, "邮箱或密码不能为空");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessException(400, "邮箱格式不正确");
        }

        // 2. 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 3. 校验用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }

        // 4. 校验密码
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "密码错误");
        }

        //5.用户脱敏
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getEmail());
        safetyUser.setPassword(user.getPassword());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setStatus(user.getStatus());
        safetyUser.setCreatedAt(new Date());
        // 6. 保存登录信息到 Session
        request.getSession().setAttribute(USER_LOGIN_STATE , user);
        return safetyUser; //这里记得要返回这个safetyUser
   }
}




