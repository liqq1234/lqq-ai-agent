package com.lqq.lqqaiagent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqq.lqqaiagent.exception.BusinessException;
import com.lqq.lqqaiagent.exception.ErrorCode;
import com.lqq.lqqaiagent.mapper.UserMapper;
import com.lqq.lqqaiagent.model.dto.user.UserQueryRequest;
import com.lqq.lqqaiagent.model.entity.User;
import com.lqq.lqqaiagent.model.enums.UserStatusEnum;
import com.lqq.lqqaiagent.model.vo.LoginUserVO;
import com.lqq.lqqaiagent.model.vo.UserVO;
import com.lqq.lqqaiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.lqq.lqqaiagent.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户表 Service 实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

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
        if (password.length() < 8|| checkPassword.length()<8) {
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
        String encryptPassword = getEncryptPassword(password);

        // 4. 保存用户
        User user = new User();
        user.setEmail(email);
        user.setUserName(username);
        user.setPassword(encryptPassword);
        userMapper.insert(user);

        return user.getId();
    }
    @Override
    public LoginUserVO userLogin(String email, String password, HttpServletRequest request) {
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
        if (user.getStatus() != null && UserStatusEnum.ENABLED.getValue().equals(user.getStatus()) ) {
            throw new BusinessException(ErrorCode.NO_AUTH, "账号已被禁用，请联系管理员");
        }

        // 4. 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 5. 保存登录信息到 Session（原始对象）
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        // 6. 返回脱敏用户
        return this.getLoginUserVO(user);
    }
    @Override
    public User getLoginUser(HttpServletRequest request) {
        //先判断是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //从数据库查询
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if(currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return  currentUser;
    }
    @Override
    public Boolean userLogout(HttpServletRequest request) {
        //先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) return null;
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }
    @Override
    public UserVO getUserVO(User user){
        if(user == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }
    @Override
    public List<UserVO> getUserVOList(List<User> userList){
        if(CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = userQueryRequest.getId();
        String email = userQueryRequest.getEmail();
        String userName = userQueryRequest.getUserName();
        String userRole = userQueryRequest.getRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        // 创建 QueryWrapper 实例
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 动态拼接查询条件（只有非空才加上）
        if (id != null) {
            queryWrapper.eq("id", id);
        }
        if (userRole != null && !userRole.isEmpty()) {
            queryWrapper.eq("role", userRole);
        }
        if (email != null && !email.isEmpty()) {
            queryWrapper.like("email", email);
        }
        if (userName != null && !userName.isEmpty()) {
            queryWrapper.like("user_name", userName);
        }
        if (sortField != null && !sortField.isEmpty()) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            queryWrapper.orderBy(true, isAsc, sortField);
        }

        return queryWrapper;
    }
    @Override
    public String getEncryptPassword(String password) {
        return passwordEncoder.encode(password);
    }
}


