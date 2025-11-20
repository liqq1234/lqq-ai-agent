package com.lqq.lqqaiagent.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 工具类
 */
public class MD5Util {

    /**
     * 生成缓存 Key（使用 MD5 避免特殊字符）
     * 
     * @param prefix 缓存前缀
     * @param content 内容
     * @return MD5 加密后的缓存 Key
     */
    public static String generateCacheKey(String prefix, String content) {
        String md5Content = md5(content);
        return prefix + md5Content;
    }

    /**
     * 计算字符串的 MD5 值
     * 
     * @param input 输入字符串
     * @return MD5 值（32位小写）
     */
    public static String md5(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 算法不可用", e);
        }
    }
}
