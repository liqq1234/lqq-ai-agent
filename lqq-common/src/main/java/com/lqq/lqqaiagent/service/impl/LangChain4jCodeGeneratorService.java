package com.lqq.lqqaiagent.service.impl;

import com.lqq.lqqaiagent.langchain4j.UniversalAssistant;
import com.lqq.lqqaiagent.model.dto.HtmlCodeResult;
import com.lqq.lqqaiagent.model.dto.MultiFileCodeResult;
import com.lqq.lqqaiagent.service.AiCodeGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 基于LangChain4j的代码生成服务实现
 * 
 * @author lqq
 * @date 2025-11-08
 */
@Slf4j
@Service
public class LangChain4jCodeGeneratorService implements AiCodeGeneratorService {
    
    private final UniversalAssistant universalAssistant;
    
    public LangChain4jCodeGeneratorService(UniversalAssistant universalAssistant) {
        this.universalAssistant = universalAssistant;
    }
    
    @Override
    public HtmlCodeResult generateHtmlCode(String userMessage) {
        try {
            log.info("使用LangChain4j生成HTML代码，用户消息: {}", userMessage);
            
            // 构造HTML生成提示
            String prompt = String.format("""
                请根据用户需求生成一个完整的HTML页面：%s
                
                要求：
                1. 生成完整的HTML结构，包含<!DOCTYPE html>、<html>、<head>、<body>标签
                2. 包含适当的CSS样式，使页面美观
                3. 如果需要交互功能，请添加JavaScript
                4. 确保代码格式正确，可以直接运行
                
                请直接返回HTML代码，不需要其他说明。
                """, userMessage);
            
            String htmlCode = universalAssistant.chat(prompt);
            
            // 提取HTML代码（去除可能的markdown标记）
            htmlCode = extractHtmlCode(htmlCode);
            
            HtmlCodeResult result = new HtmlCodeResult();
            result.setHtmlCode(htmlCode);
            result.setSuccess(true);
            result.setMessage("HTML代码生成成功");
            
            log.info("HTML代码生成完成，长度: {} 字符", htmlCode.length());
            return result;
            
        } catch (Exception e) {
            log.error("生成HTML代码失败", e);
            
            HtmlCodeResult result = new HtmlCodeResult();
            result.setSuccess(false);
            result.setMessage("生成失败: " + e.getMessage());
            result.setHtmlCode("<html><body><h1>生成失败</h1><p>" + e.getMessage() + "</p></body></html>");
            
            return result;
        }
    }
    
    @Override
    public MultiFileCodeResult generateMultiFileCode(String userMessage) {
        try {
            log.info("使用LangChain4j生成多文件项目，用户消息: {}", userMessage);
            
            // 构造多文件项目生成提示
            String prompt = String.format("""
                请根据用户需求生成一个完整的项目：%s
                
                要求：
                1. 生成项目所需的所有文件
                2. 包含适当的目录结构
                3. 每个文件都要有完整的代码
                4. 提供README.md说明文件
                
                请按以下格式返回：
                文件名1:
                ```
                文件内容1
                ```
                
                文件名2:
                ```
                文件内容2
                
                ```
                """, userMessage);
            
            String response = universalAssistant.chat(prompt);
            
            MultiFileCodeResult result = new MultiFileCodeResult();
            result.setSuccess(true);
            result.setMessage("多文件项目生成成功");
            result.setProjectStructure(response);
            
            log.info("多文件项目生成完成");
            return result;
            
        } catch (Exception e) {
            log.error("生成多文件项目失败", e);
            
            MultiFileCodeResult result = new MultiFileCodeResult();
            result.setSuccess(false);
            result.setMessage("生成失败: " + e.getMessage());
            result.setProjectStructure("生成失败: " + e.getMessage());
            
            return result;
        }
    }
    
    /**
     * 提取HTML代码，去除markdown标记
     */
    private String extractHtmlCode(String response) {
        if (response == null) {
            return "";
        }
        
        // 去除可能的markdown代码块标记
        String cleaned = response.trim();
        
        // 如果包含```html标记，提取其中的内容
        if (cleaned.contains("```html")) {
            int start = cleaned.indexOf("```html") + 7;
            int end = cleaned.lastIndexOf("```");
            if (end > start) {
                cleaned = cleaned.substring(start, end).trim();
            }
        } else if (cleaned.contains("```")) {
            // 如果只有```标记
            int start = cleaned.indexOf("```") + 3;
            int end = cleaned.lastIndexOf("```");
            if (end > start) {
                cleaned = cleaned.substring(start, end).trim();
            }
        }
        
        return cleaned;
    }
}
