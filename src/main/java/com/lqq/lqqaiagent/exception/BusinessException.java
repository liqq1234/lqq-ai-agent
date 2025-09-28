package com.lqq.lqqaiagent.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final String message;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
