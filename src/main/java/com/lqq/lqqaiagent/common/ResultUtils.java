package com.lqq.lqqaiagent.common;

/**
 * 返回工具类
 *
 * @author lqq
 */
public class ResultUtils {
    /**
     * 成功返回
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"ok");
    }
    /**
     * 失败返回
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
    }

}
