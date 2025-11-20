package com.lqq.lqqaiagent.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户角色：user/admin
     */
    private String role;
    /**
     * 状态：disabled-禁用，enabled-启用
     */
    private String status;

    private static final long serialVersionUID = 1L;
}
