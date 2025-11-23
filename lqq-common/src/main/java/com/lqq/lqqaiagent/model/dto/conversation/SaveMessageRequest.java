package com.lqq.lqqaiagent.model.dto.conversation;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 保存消息请求
 */
@Data
public class SaveMessageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 对话ID
     */
    @NotNull(message = "对话ID不能为空")
    private Long conversationId;

    /**
     * 消息角色：user 或 assistant
     */
    @NotBlank(message = "消息角色不能为空")
    private String role;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
}
