package com.lqq.lqqaiagent.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PromptLoader {

    /**
     * 从 resources 中加载指定文件名的 prompt 内容
     * @param fileName 文件名，例如 "codegen-html-system-prompt.txt"
     * @return 文件内容字符串
     */
    public static String loadPrompt(String fileName) {
        try (InputStream inputStream = PromptLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("找不到文件: " + fileName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("加载 prompt 文件失败: " + fileName, e);
        }
    }
}
