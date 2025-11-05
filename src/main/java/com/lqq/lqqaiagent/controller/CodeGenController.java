package com.lqq.lqqaiagent.controller;

import com.lqq.lqqaiagent.model.enums.CodeGenTypeEnum;
import com.lqq.lqqaiagent.service.facade.AiCodeGeneratorFacade;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/codegen")
public class CodeGenController {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    /**
     * 生成 HTML 模式代码并保存
     */
    @PostMapping("/html")
    public String generateHtml(@RequestParam String userMessage) {
        File savedDir = aiCodeGeneratorFacade.generateAndSaveCode(userMessage, CodeGenTypeEnum.HTML);
        return "HTML 代码已生成并保存到：" + savedDir.getAbsolutePath();
    }

    /**
     * 生成多文件项目并保存
     */
    @PostMapping("/project")
    public String generateProject(@RequestParam String userMessage) {
        File savedDir = aiCodeGeneratorFacade.generateAndSaveCode(userMessage, CodeGenTypeEnum.MULTI_FILE);
        return "多文件项目已生成并保存到：" + savedDir.getAbsolutePath();
    }




}
