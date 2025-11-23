package com.lqq.lqqaiagent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqq.lqqaiagent.exception.ErrorCode;
import com.lqq.lqqaiagent.exception.BusinessException;
import com.lqq.lqqaiagent.mapper.ConversationMapper;
import com.lqq.lqqaiagent.mapper.MessageMapper;
import com.lqq.lqqaiagent.model.dto.conversation.SaveMessageRequest;
import com.lqq.lqqaiagent.model.entity.Conversation;
import com.lqq.lqqaiagent.model.entity.Message;
import com.lqq.lqqaiagent.model.vo.ConversationDetailVO;
import com.lqq.lqqaiagent.model.vo.ConversationVO;
import com.lqq.lqqaiagent.model.vo.MessageVO;
import com.lqq.lqqaiagent.service.ConversationService;
import com.lqq.lqqaiagent.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话服务实现类
 */
@Slf4j
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation>
        implements ConversationService {

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConversation() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle("新对话");
        // 避免数据库 NOT NULL 约束报错：显式设置创建/更新时间
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());

        boolean success = this.save(conversation);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建对话失败");
        }

        log.info("用户 {} 创建新对话: {}", userId, conversation.getId());
        return conversation.getId();
    }

    @Override
    public List<ConversationVO> listUserConversations() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 使用单条 SQL 批量查询：对话基础信息 + 消息数量 + 最后一条消息
        List<ConversationVO> conversations = conversationMapper.listUserConversations(userId);
        if (conversations == null) {
            return Collections.emptyList();
        }
        return conversations;
    }

    @Override
    public ConversationDetailVO getConversationDetail(Long conversationId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 查询对话
        Conversation conversation = this.getById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "对话不存在");
        }

        // 检查权限
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权访问此对话");
        }

        // 构建 VO
        ConversationDetailVO vo = new ConversationDetailVO();
        BeanUtil.copyProperties(conversation, vo);

        // 查询所有消息
        List<Message> messages = messageMapper.selectByConversationId(conversationId);
        List<MessageVO> messageVOs = messages.stream().map(message -> {
            MessageVO messageVO = new MessageVO();
            BeanUtil.copyProperties(message, messageVO);
            // 设置时间戳（前端使用）
            messageVO.setTimestamp(message.getCreateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());
            return messageVO;
        }).collect(Collectors.toList());

        vo.setMessages(messageVOs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMessage(SaveMessageRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 验证对话是否存在且属于当前用户
        Conversation conversation = this.getById(request.getConversationId());
        if (conversation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "对话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权操作此对话");
        }

        // 保存消息
        Message message = new Message();
        message.setConversationId(request.getConversationId());
        message.setUserId(userId);
        message.setRole(request.getRole());
        message.setContent(request.getContent());
        // 显式设置时间，避免 NOT NULL 约束
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());

        int result = messageMapper.insert(message);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存消息失败");
        }

        // 如果是第一条消息，使用消息内容作为对话标题
        int messageCount = messageMapper.countByConversationId(request.getConversationId());
        if (messageCount == 1 && "user".equals(request.getRole())) {
            String title = request.getContent();
            if (title.length() > 20) {
                title = title.substring(0, 20) + "...";
            }
            this.updateTitle(request.getConversationId(), title);
        }

        log.info("保存消息成功: conversationId={}, role={}", request.getConversationId(), request.getRole());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long conversationId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 验证对话是否存在且属于当前用户
        Conversation conversation = this.getById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "对话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权删除此对话");
        }

        // 逻辑删除对话
        boolean success = this.removeById(conversationId);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除对话失败");
        }

        log.info("删除对话成功: conversationId={}, userId={}", conversationId, userId);
    }

    @Override
    public void updateTitle(Long conversationId, String title) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        Conversation conversation = this.getById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "对话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权修改此对话");
        }

        conversation.setTitle(title);
        this.updateById(conversation);
    }
}
