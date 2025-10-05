package com.lqq.lqqaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 用户表
 * TableName  users
 */
@TableName(value ="users")
@Data
public class User {
    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户角色："user"-普通用户，"admin"-管理员
     */
    private String role;

    /**
     * 状态：disabled-禁用，enabled-启用
     */
    private String status;

    /**
     * 注册时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 最后登录时间
     */
    private Date lastLogin;
    /**
     * 逻辑删除字段：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}