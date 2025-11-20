package com.lqq.lqqaiagent.langchain4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 简化的Agent控制器
 * 使用LangChain4j实现，代码量极少但功能强大
 * 
 * @author lqq
 * @date 2025-11-08
 */
@Slf4j
@RestController
@RequestMapping("/api/simple")
@CrossOrigin(originPatterns = "*")
public class SimpleAgentController {
    
    private final HtmlAssistant htmlAssistant;
    private final UniversalAssistant universalAssistant;
    
    public SimpleAgentController(HtmlAssistant htmlAssistant, UniversalAssistant universalAssistant) {
        this.htmlAssistant = htmlAssistant;
        this.universalAssistant = universalAssistant;
    }
    
    /**
     * HTML页面创建专用接口
     */
    @PostMapping("/html")
    public ResponseEntity<String> createHtml(@RequestParam String message) {
        try {
            log.info("收到HTML创建请求: {}", message);
            String result = htmlAssistant.createPage(message);
            log.info("HTML创建完成");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("HTML创建失败", e);
            return ResponseEntity.status(500).body("❌ 处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 通用智能助手接口
     * 可以处理各种任务：文件创建、代码生成、问题解答等
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam String message) {
        try {
            log.info("收到通用请求: {}", message);
            String result = universalAssistant.chat(message);
            log.info("请求处理完成");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("请求处理失败", e);
            return ResponseEntity.status(500).body("❌ 处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("🚀 LangChain4j Agent 运行正常！");
    }
    
    /**
     * 获取Agent能力说明
     */
    @GetMapping("/capabilities")
    public ResponseEntity<String> getCapabilities() {
        String capabilities = """
            🤖 LangChain4j Agent 能力说明：
            
            📝 HTML页面创建：
            - 创建基础HTML页面
            - 创建带样式的精美页面
            - 自动添加响应式设计
            
            📁 文件操作：
            - 创建各种类型的文本文件
            - 读取文件内容
            - 追加内容到文件
            - 生成Java类文件
            
            💬 智能对话：
            - 回答各种问题
            - 提供技术建议
            - 代码生成和解释
            
            🎯 使用方式：
            - POST /api/simple/html?message=创建一个登录页面
            - POST /api/simple/chat?message=帮我写一个Java工具类
            
            ✨ 特点：零配置、自动工具调用、一次完成任务！
            """;
        return ResponseEntity.ok(capabilities);
    }
}
