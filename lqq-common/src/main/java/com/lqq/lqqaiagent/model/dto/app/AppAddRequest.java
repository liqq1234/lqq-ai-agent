package com.lqq.lqqaiagent.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {
    /** 应用名称 */
    private String appName;
    /** 应用封面，可选 */
    private String cover;
    /** 初始化 prompt（必填） */
    private String initPrompt;
    /** 代码生成类型（可选） */
    private String codeGenType;
}
