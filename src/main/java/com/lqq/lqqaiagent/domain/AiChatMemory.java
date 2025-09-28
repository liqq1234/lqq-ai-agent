package com.lqq.lqqaiagent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("ai_chat_memory")
public class AiChatMemory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String chatId;

    private String type;

    private String content;

    private Date createTime;

    private Date updateTime;

    private Integer isDel;

}