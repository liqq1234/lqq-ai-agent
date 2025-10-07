package com.lqq.lqqaiagent.controller;

import com.lqq.lqqaiagent.model.dto.CodeGenResult;
import com.lqq.lqqaiagent.model.entity.User;
import com.lqq.lqqaiagent.service.impl.DashScopeChatService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.lqq.lqqaiagent.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/codegen")
public class CodeGenController {

    @Resource
    private DashScopeChatService chatService;

    @PostMapping("/html")
    public CodeGenResult generateHtml(HttpServletRequest request, @RequestParam String userInput) {
        boolean loggedIn = isLoggedIn(request);
        return chatService.generateHtml(userInput, loggedIn);
    }

    @PostMapping("/project")
    public CodeGenResult generateProject(HttpServletRequest request, @RequestParam String userInput) {
        boolean loggedIn = isLoggedIn(request);
        return chatService.generateProject(userInput, loggedIn);
    }

    private Boolean isLoggedIn(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj instanceof User currentUser && currentUser.getId() != null) {
            return true;
        }
        return false;
    }
}
