package com.lqq.lqqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserVO implements Serializable {

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
    private  String role;
    /**
     * 注册时间
     */
    private Date createdAt;

}
