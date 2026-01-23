package com.fontal.fonpicturebackend.exception;

/**
 * 异常处理工具类
 */
public class ThrowUtils {
    /**
     * 条件成立抛出异常
     *
     * @param condition 条件
     * @param runtimeException 异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if(condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立抛出异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        if(condition) {
            throw new  BusinessException(errorCode);
        }
    }

    /**
     * 条件成立抛出异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param message 消息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        if(condition) {
            throw new  BusinessException(errorCode,message);
        }
    }
}
