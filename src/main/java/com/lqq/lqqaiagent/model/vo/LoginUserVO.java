package com.lqq.lqqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LoginUserVO implements Serializable {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String userName;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 用户角色（admin / user） */
    private String role;

    /** 用户状态（0-正常，1-禁用 ） */
    private String status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

    /** 上次登录时间 */
    private Date lastLogin;

    private static final long serialVersionUID = 1L;

}
