package com.lqq.lqqaiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lqq.lqqaiagent.model.dto.user.UserQueryRequest;
import com.lqq.lqqaiagent.model.entity.User;
import com.lqq.lqqaiagent.model.vo.LoginUserVO;
import com.lqq.lqqaiagent.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务接口
 * <p>
 * 该接口定义了针对【users（用户表）】的业务逻辑操作，
 * 包括用户注册、登录、注销、信息脱敏、登录状态查询、条件查询等功能。
 * 所有数据库的增删改查操作由 MyBatis-Plus 的 IService<User> 提供基础支持。
 *
 * @author LQQ
 * @since 2025-09-26
 */
@Service
public interface UserService extends IService<User> {

    /**
     * 用户注册功能
     * <p>
     * 校验邮箱和密码的合法性，确认两次密码一致后，插入数据库新用户记录。
     *
     * @param email         用户邮箱
     * @param username      用户名
     * @param password      用户密码
     * @param checkPassword 二次确认密码
     * @return 新注册用户的 ID
     */
    long userRegister(String email, String username, String password, String checkPassword);

    /**
     * 用户登录功能
     * <p>
     * 校验邮箱与密码是否匹配，并在登录成功后将登录态保存到 Session 中。
     *
     * @param email   用户邮箱
     * @param password 用户密码
     * @param request
     * @return 登录成功后的用户信息
     */
    LoginUserVO userLogin(String email, String password, HttpServletRequest request);

    /**
     * 获取当前已登录用户信息
     * <p>
     * 从 Session 中获取当前登录用户的详细信息。
     *
     * @param request HTTP 请求对象，用于读取 Session
     * @return 当前登录用户实体对象（User）
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销功能
     * <p>
     * 清除当前用户的登录态信息，使其退出登录状态。
     *
     * @param request HTTP 请求对象，用于操作 Session
     * @return 是否注销成功（true 为成功）
     */
    Boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的登录用户信息
     * <p>
     * 将用户敏感信息（如密码等）移除，仅保留可安全展示的部分。
     *
     * @param user 用户实体对象
     * @return 脱敏后的登录用户视图对象（LoginUserVO）
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏后的用户信息
     * <p>
     * 用于普通用户数据展示（如用户列表），与登录态无关。
     *
     * @param user 用户实体对象
     * @return 用户视图对象（UserVO）
     */
    UserVO getUserVO(User user);

    /**
     * 批量获取脱敏后的用户信息列表
     * <p>
     * 将用户实体列表统一转换为可展示的视图对象列表。
     *
     * @param userList 用户实体对象列表
     * @return 用户视图对象列表（UserVO List）
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 构建用户查询条件
     * <p>
     * 根据前端传入的 UserQueryRequest 参数动态生成数据库查询条件，
     * 支持 ID、邮箱、用户名、角色等字段的精准或模糊匹配，并可附加排序条件。
     *
     * @param userQueryRequest 用户查询请求参数对象
     * @return 构造好的 QueryWrapper 对象（用于 MyBatis-Plus 查询）
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    String getEncryptPassword(String password);
}
