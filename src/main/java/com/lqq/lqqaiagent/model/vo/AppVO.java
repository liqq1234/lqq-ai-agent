package com.lqq.lqqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AppVO implements Serializable {
    /** id */
    private Long id;
    /** 应用名称 */
    private String appName;
    /** 应用封面 */
    private String cover;
    /** 优先级 */
    private Integer priority;
    /** 创建用户id */
    private Long userId;
    /** 创建时间 */
    private Date createTime;
    /** 编辑时间 */
    private Date editTime;
    /** 更新时间 */
    private Date updateTime;
}
