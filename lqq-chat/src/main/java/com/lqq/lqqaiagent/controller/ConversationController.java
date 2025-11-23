package com.lqq.lqqaiagent.controller;

import com.lqq.lqqaiagent.common.BaseResponse;
import com.lqq.lqqaiagent.common.ResultUtils;
import com.lqq.lqqaiagent.model.dto.conversation.SaveMessageRequest;
import com.lqq.lqqaiagent.model.vo.ConversationDetailVO;
import com.lqq.lqqaiagent.model.vo.ConversationVO;
import com.lqq.lqqaiagent.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 对话控制器
 */
@Slf4j
@RestController
@RequestMapping("/conversation")
@Tag(name = "对话管理")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    /**
     * 创建新对话
     */
    @PostMapping("/create")
    @Operation(summary = "创建新对话")
    public BaseResponse<Long> createConversation() {
        log.info("创建新对话");
        Long conversationId = conversationService.createConversation();
        return ResultUtils.success(conversationId);
    }

    /**
     * 获取用户的对话列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取对话列表")
    public BaseResponse<List<ConversationVO>> listConversations() {
        log.info("获取对话列表");
        List<ConversationVO> conversations = conversationService.listUserConversations();
        return ResultUtils.success(conversations);
    }

    /**
     * 获取对话详情（包含消息列表）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取对话详情")
    public BaseResponse<ConversationDetailVO> getConversationDetail(@PathVariable Long id) {
        log.info("获取对话详情: {}", id);
        ConversationDetailVO detail = conversationService.getConversationDetail(id);
        return ResultUtils.success(detail);
    }

    /**
     * 保存消息
     */
    @PostMapping("/message/save")
    @Operation(summary = "保存消息")
    public BaseResponse<Void> saveMessage(@Valid @RequestBody SaveMessageRequest request) {
        log.info("保存消息: conversationId={}, role={}", request.getConversationId(), request.getRole());
        conversationService.saveMessage(request);
        return ResultUtils.success(null);
    }

    /**
     * 删除对话
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除对话")
    public BaseResponse<Void> deleteConversation(@PathVariable Long id) {
        log.info("删除对话: {}", id);
        conversationService.deleteConversation(id);
        return ResultUtils.success(null);
    }

    /**
     * 更新对话标题
     */
    @PutMapping("/{id}/title")
    @Operation(summary = "更新对话标题")
    public BaseResponse<Void> updateTitle(@PathVariable Long id, @RequestParam String title) {
        log.info("更新对话标题: id={}, title={}", id, title);
        conversationService.updateTitle(id, title);
        return ResultUtils.success(null);
    }
}
