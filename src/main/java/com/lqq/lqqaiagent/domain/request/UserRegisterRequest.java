package com.lqq.lqqaiagent.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {
    public static final long serialVersionUID = -2394960940159547277L;

    private String username;

    private String password;

    private String checkPassword;


}
