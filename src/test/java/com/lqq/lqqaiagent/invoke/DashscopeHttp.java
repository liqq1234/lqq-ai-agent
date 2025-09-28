package com.lqq.lqqaiagent.invoke;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;

/**
 * 使用 Hutool 调用阿里云 DashScope API 的示例
 * 等价于 curl 命令的 Java 实现
 */
public class DashscopeHttp{
    public static String callWithHutool() {
        // 从环境变量或系统属性读取 API Key
        String apiKey = TestApiKey.API_KEY;
        // 构建消息
        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "You are a helpful assistant.");

        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", "帮我说一下晚上吃什么");

        List<Map<String, Object>> messages = Arrays.asList(systemMsg, userMsg);

        // 构建请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "qwen-plus");
        payload.put("messages", messages);

        String body = JSONUtil.toJsonStr(payload);

        // 发起 HTTP 请求
        String url = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

        HttpResponse resp = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .contentType("application/json")
                .body(body)
                .timeout(30_000)
                .execute();

        if (resp == null) {
            throw new RuntimeException("No response from server");
        }

        int status = resp.getStatus();
        String respBody = resp.body();
        if (status >= 200 && status < 300) {
            return respBody;
        } else {
            throw new RuntimeException("HTTP " + status + ": " + respBody);
        }
    }

    /**
     * 解析响应并提取助手回答
     */
    public static String extractAnswer(String jsonResponse) {
        try {
            Map<String, Object> response = JSONUtil.toBean(jsonResponse, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    return (String) message.get("content");
                }
            }
            return "无法解析响应";
        } catch (Exception e) {
            return "解析响应时出错: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("正在调用 DashScope API...");
            String result = callWithHutool();
            System.out.println("完整响应:");
            System.out.println(result);

            System.out.println("\n助手回答:");
            String answer = extractAnswer(result);
            System.out.println(answer);

        } catch (Exception e) {
            System.err.println("调用 DashScope API 时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
