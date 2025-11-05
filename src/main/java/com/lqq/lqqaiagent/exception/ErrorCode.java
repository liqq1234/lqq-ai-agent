package com.lqq.lqqaiagent.exception;

import lombok.Getter;

/**
 * 错误码
 *
 * @author lqq
 */
@Getter
public enum ErrorCode {

    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001,"请求参数为空",""),
    NOT_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    NOT_FOUND_ERROR(40400, "未找到对应资源", ""),
    SYSTEM_ERROR(50000, "系统内部异常", ""),
    OPERATION_ERROR(50001, "操作失败","");

    private final int code;
    private final String message;
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
}
