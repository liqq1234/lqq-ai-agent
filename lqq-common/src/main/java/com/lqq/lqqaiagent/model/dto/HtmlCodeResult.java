package com.lqq.lqqaiagent.model.dto;

import lombok.Data;

/**
 * HTMLCodeResult
 * @author lqq
 */
@Data
public class HtmlCodeResult {
    /**
     * HTML代码
     */
    private String htmlCode;

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
}
