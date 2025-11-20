package com.lqq.lqqaiagent.util;

/**
 * 加载系统 prompt 的工具类
 */
public class CodeGenPrompts {

    // HTML prompt
    public static final String HTML_PROMPT = PromptLoader.loadPrompt("prompt/codegen-html-system-prompt.txt");

    // 多文件项目 prompt
    public static final String MULTI_FILE_PROMPT = PromptLoader.loadPrompt("prompt/codegen-multi-file-system-prompt.txt");

}
