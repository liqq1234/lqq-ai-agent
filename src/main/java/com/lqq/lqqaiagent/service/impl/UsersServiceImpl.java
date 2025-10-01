package com.lqq.lqqaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqq.lqqaiagent.common.ErrorCode;
import com.lqq.lqqaiagent.domain.User;
import com.lqq.lqqaiagent.exception.BusinessException;
import com.lqq.lqqaiagent.mapper.UserMapper;
import com.lqq.lqqaiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import static com.lqq.lqqaiagent.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户表 Service 实现
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public long userRegister(String email, String username, String password, String checkPassword) {
        // 1. 参数校验
        if (StringUtils.isAnyBlank(email, username, password, checkPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "注册参数缺失");
        }
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不一致");
        }
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于8位");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 2. 检查邮箱是否已注册
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User existUser = userMapper.selectOne(queryWrapper);
        if (existUser != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱已被注册");
        }

        // 3. 密码加密
        String encryptPassword = passwordEncoder.encode(password);

        // 4. 保存用户
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(encryptPassword);
        userMapper.insert(user);

        return user.getId();
    }

    @Override
    public User userLogin(String email, String password, HttpServletRequest request) {
        // 1. 参数校验
        if (StringUtils.isBlank(email) || StringUtils.isBlank(password)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "邮箱或密码不能为空");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 2. 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }

        // 3. 校验用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.NO_AUTH, "账号已被禁用，请联系管理员");
        }

        // 4. 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 5. 保存登录信息到 Session（原始对象）
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        // 6. 返回脱敏用户
        return toSafetyUser(user);
    }

    @Override
    public Integer userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public User toSafetyUser(User user) {
        if (user == null) return null;

        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setRole(user.getRole());
        safetyUser.setStatus(user.getStatus());
        safetyUser.setCreatedAt(user.getCreatedAt());
        safetyUser.setUpdatedAt(user.getUpdatedAt());
        safetyUser.setLastLogin(user.getLastLogin());
        safetyUser.setDeleted(user.getDeleted());
        // 注意：不要把密码放入脱敏对象
        return safetyUser;
    }
}
