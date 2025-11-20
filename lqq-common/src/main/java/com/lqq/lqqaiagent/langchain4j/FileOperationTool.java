package com.lqq.lqqaiagent.langchain4j;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 文件操作工具集
 * 
 * @author lqq
 * @date 2025-11-08
 */
@Slf4j
@Component
public class FileOperationTool {
    
    @Tool("创建文本文件")
    public String createTextFile(
        @P("文件内容") String content,
        @P("文件名，包含扩展名") String filename
    ) {
        try {
            Path path = Paths.get(filename);
            Files.writeString(path, content, StandardCharsets.UTF_8);
            
            return "✅ 文本文件创建成功！\n" +
                   "📁 文件: " + path.toAbsolutePath() + "\n" +
                   "📄 大小: " + Files.size(path) + " 字节";
                   
        } catch (IOException e) {
            return "❌ 创建文件失败: " + e.getMessage();
        }
    }
    
    @Tool("读取文件内容")
    public String readFile(@P("文件路径") String filepath) {
        try {
            Path path = Paths.get(filepath);
            if (!Files.exists(path)) {
                return "❌ 文件不存在: " + filepath;
            }
            
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return "📄 文件内容:\n" + content;
            
        } catch (IOException e) {
            return "❌ 读取文件失败: " + e.getMessage();
        }
    }
    
    @Tool("追加内容到文件")
    public String appendToFile(
        @P("要追加的内容") String content,
        @P("文件路径") String filepath
    ) {
        try {
            Path path = Paths.get(filepath);
            Files.writeString(path, content, StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            
            return "✅ 内容已追加到文件: " + path.toAbsolutePath();
            
        } catch (IOException e) {
            return "❌ 追加内容失败: " + e.getMessage();
        }
    }
    
    @Tool("创建Java类文件")
    public String createJavaClass(
        @P("类名") String className,
        @P("包名") String packageName,
        @P("类的功能描述") String description
    ) {
        try {
            String javaCode = """
                package %s;
                
                /**
                 * %s
                 * 
                 * @author AI Generated
                 * @date %s
                 */
                public class %s {
                    
                    /**
                     * 构造方法
                     */
                    public %s() {
                        // TODO: 实现构造逻辑
                    }
                    
                    /**
                     * 主要业务方法
                     */
                    public void doSomething() {
                        // TODO: 实现业务逻辑
                    }
                }
                """.formatted(
                    packageName, 
                    description, 
                    java.time.LocalDate.now(), 
                    className, 
                    className
                );
            
            String filename = className + ".java";
            Path path = Paths.get(filename);
            Files.writeString(path, javaCode, StandardCharsets.UTF_8);
            
            return "✅ Java类文件创建成功！\n" +
                   "📁 文件: " + path.toAbsolutePath() + "\n" +
                   "☕ 包含基础的类结构和注释";
                   
        } catch (IOException e) {
            return "❌ 创建Java类失败: " + e.getMessage();
        }
    }
}
