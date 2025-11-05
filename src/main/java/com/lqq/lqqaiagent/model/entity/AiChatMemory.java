package com.lqq.lqqaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName ai_chat_memory
 */
@TableName(value ="ai_chat_memory")
@Data
public class AiChatMemory {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 会话id
     */
    private String chatId;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除标记,0-未删除;1-已删除
     */
    private Integer isDel;
}