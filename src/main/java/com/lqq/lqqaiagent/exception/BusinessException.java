package com.lqq.lqqaiagent.exception;

/**
 * 自定义业务异常
 */
public class BusinessException extends RuntimeException {
    private final int code;  // 错误码
    private final String message; // 错误信息

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
