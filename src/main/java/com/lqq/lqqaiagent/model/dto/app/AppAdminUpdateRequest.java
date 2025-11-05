package com.lqq.lqqaiagent.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppAdminUpdateRequest implements Serializable {
    /** 应用id */
    private Long id;
    /** 应用名称 */
    private String appName;
    /** 应用封面 */
    private String cover;
    /** 优先级 */
    private Integer priority;
}
