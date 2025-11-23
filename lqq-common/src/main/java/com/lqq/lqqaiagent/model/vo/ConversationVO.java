package com.lqq.lqqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话视图对象
 */
@Data
public class ConversationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 对话ID
     */
    private Long id;

    /**
     * 对话标题
     */
    private String title;

    /**
     * 最后一条消息
     */
    private String lastMessage;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
