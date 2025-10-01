package com.lqq.lqqaiagent.exception;

import com.lqq.lqqaiagent.common.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private  int code;
    private  String description;

    public BusinessException(String message,int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }
    public BusinessException(ErrorCode errorCode,String description){
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
}
