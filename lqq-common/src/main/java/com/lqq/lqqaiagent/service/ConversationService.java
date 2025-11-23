package com.lqq.lqqaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqq.lqqaiagent.model.dto.conversation.SaveMessageRequest;
import com.lqq.lqqaiagent.model.entity.Conversation;
import com.lqq.lqqaiagent.model.vo.ConversationDetailVO;
import com.lqq.lqqaiagent.model.vo.ConversationVO;

import java.util.List;

/**
 * 对话服务
 */
public interface ConversationService extends IService<Conversation> {

    /**
     * 创建新对话
     *
     * @return 对话ID
     */
    Long createConversation();

    /**
     * 获取用户的对话列表
     *
     * @return 对话列表
     */
    List<ConversationVO> listUserConversations();

    /**
     * 获取对话详情（包含所有消息）
     *
     * @param conversationId 对话ID
     * @return 对话详情
     */
    ConversationDetailVO getConversationDetail(Long conversationId);

    /**
     * 保存消息
     *
     * @param request 保存消息请求
     */
    void saveMessage(SaveMessageRequest request);

    /**
     * 删除对话
     *
     * @param conversationId 对话ID
     */
    void deleteConversation(Long conversationId);

    /**
     * 更新对话标题
     *
     * @param conversationId 对话ID
     * @param title          新标题
     */
    void updateTitle(Long conversationId, String title);
}
