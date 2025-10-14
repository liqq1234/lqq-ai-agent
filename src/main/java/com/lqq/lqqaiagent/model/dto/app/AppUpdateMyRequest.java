package com.lqq.lqqaiagent.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppUpdateMyRequest implements Serializable {
    /** 应用id */
    private Long id;
    /** 应用名称（仅支持修改此字段） */
    private String appName;
}
