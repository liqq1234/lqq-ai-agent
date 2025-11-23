package com.lqq.lqqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话详情视图对象
 */
@Data
public class ConversationDetailVO implements Serializable {

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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 消息列表
     */
    private List<MessageVO> messages;
}
