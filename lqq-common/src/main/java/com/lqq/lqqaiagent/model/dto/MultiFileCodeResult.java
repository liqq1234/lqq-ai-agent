package com.lqq.lqqaiagent.model.dto;

import lombok.Data;

/**
 * MultiFileCodeResult
 * 
 * 表示 AI 生成的多文件代码结果，包括 HTML、CSS、JS 代码以及描述信息。
 * 主要用于将 AI 生成的代码结果返回给前端。
 * 
 * @author lqq
 */
@Data
public class MultiFileCodeResult {

    /**
     * HTML 代码内容
     */
    private String htmlCode;

    /**
     * CSS 代码内容
     */
    private String cssCode;

    /**
     * JS 代码内容
     */
    private String jsCode;

    /**
     * 生成代码的描述
     */
    private String description;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 项目结构
     */
    private String projectStructure;
}
