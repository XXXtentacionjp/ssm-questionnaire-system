package com.ssm.questionnaire.common;

/**
 * 业务异常，由 GlobalExceptionHandler 统一转成响应体
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(ResultCode.PARAM_ERROR, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
