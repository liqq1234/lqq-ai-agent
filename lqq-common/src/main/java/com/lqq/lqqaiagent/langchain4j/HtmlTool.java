package com.lqq.lqqaiagent.langchain4j;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * HTML文件创建工具
 * 
 * @author lqq
 * @date 2025-11-08
 */
@Slf4j
@Component
public class HtmlTool {
    
    @Tool("创建HTML文件")
    public String createHtmlFile(
        @P("HTML内容，可以是完整的HTML或者只是body内容") String content,
        @P("文件名，默认index.html") String filename
    ) {
        try {
            log.info("开始创建HTML文件，文件名: {}", filename);
            
            // 设置默认文件名
            if (filename == null || filename.isEmpty()) {
                filename = "index.html";
            }
            
            // 如果不是完整的HTML，则包装成完整格式
            String htmlContent;
            if (content.contains("<html>") || content.contains("<!DOCTYPE")) {
                htmlContent = content;
            } else {
                htmlContent = """
                    <!DOCTYPE html>
                    <html lang="zh-CN">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>生成的页面</title>
                        <style>
                            body { font-family: Arial, sans-serif; margin: 20px; }
                            h1 { color: #333; }
                            .container { max-width: 800px; margin: 0 auto; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            %s
                        </div>
                    </body>
                    </html>
                    """.formatted(content);
            }
            
            // 创建文件
            Path path = Paths.get(filename);
            Files.writeString(path, htmlContent, StandardCharsets.UTF_8);
            
            String result = "✅ HTML文件创建成功！\n" +
                          "📁 文件路径: " + path.toAbsolutePath() + "\n" +
                          "📄 文件大小: " + Files.size(path) + " 字节\n" +
                          "🌐 可以在浏览器中打开查看效果";
            
            log.info("HTML文件创建成功: {}", path.toAbsolutePath());
            return result;
            
        } catch (Exception e) {
            String error = "❌ 创建HTML文件失败: " + e.getMessage();
            log.error("创建HTML文件失败", e);
            return error;
        }
    }
    
    @Tool("创建带样式的HTML页面")
    public String createStyledHtmlFile(
        @P("页面标题") String title,
        @P("页面内容") String content,
        @P("CSS样式，可选") String customCss,
        @P("文件名，默认styled.html") String filename
    ) {
        try {
            if (filename == null || filename.isEmpty()) {
                filename = "styled.html";
            }
            
            String defaultCss = """
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    line-height: 1.6;
                    margin: 0;
                    padding: 20px;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    min-height: 100vh;
                }
                .container {
                    max-width: 800px;
                    margin: 0 auto;
                    background: white;
                    padding: 30px;
                    border-radius: 10px;
                    box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                }
                h1 {
                    color: #333;
                    text-align: center;
                    margin-bottom: 30px;
                    font-size: 2.5em;
                }
                p {
                    color: #666;
                    font-size: 1.1em;
                    margin-bottom: 20px;
                }
                """;
            
            String finalCss = customCss != null && !customCss.isEmpty() ? 
                defaultCss + "\n" + customCss : defaultCss;
            
            String htmlContent = """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        %s
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>%s</h1>
                        %s
                    </div>
                </body>
                </html>
                """.formatted(title, finalCss, title, content);
            
            Path path = Paths.get(filename);
            Files.writeString(path, htmlContent, StandardCharsets.UTF_8);
            
            return "✅ 带样式的HTML页面创建成功！\n" +
                   "📁 文件: " + path.toAbsolutePath() + "\n" +
                   "🎨 包含精美样式和响应式设计";
                   
        } catch (Exception e) {
            return "❌ 创建样式化HTML失败: " + e.getMessage();
        }
    }
}
