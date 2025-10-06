package com.lqq.lqqaiagent.controller;

import com.lqq.lqqaiagent.model.dto.CodeGenResult;
import com.lqq.lqqaiagent.service.impl.DashScopeChatService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/codegen")
public class CodeGenController {

    @Resource
    private DashScopeChatService chatService;

    @PostMapping("/html")
    public CodeGenResult generateHtml(@RequestParam String userInput) {
        return chatService.generateHtml(userInput);
    }

    @PostMapping("/project")
    public CodeGenResult generateProject(@RequestParam String userInput) {
        return chatService.generateProject(userInput);
    }
}
