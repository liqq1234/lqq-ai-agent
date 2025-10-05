package com.lqq.lqqaiagent.model.dto.user;

import com.lqq.lqqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String email;

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
