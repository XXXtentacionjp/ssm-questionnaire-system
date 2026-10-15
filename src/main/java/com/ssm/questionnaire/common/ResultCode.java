package com.ssm.questionnaire.common;

/**
 * 响应码，和 HTTP 状态码保持同一套语义，便于前端拦截
 */
public final class ResultCode {

    public static final int OK = 200;
    public static final int PARAM_ERROR = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int SYSTEM_ERROR = 500;

    private ResultCode() {
    }
}
