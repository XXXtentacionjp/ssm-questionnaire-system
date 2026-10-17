package com.ssm.questionnaire.common;

/**
 * 统一响应体，所有接口都返回 {"code":..,"message":..,"data":..}
 */
public class Result<T> {

    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(ResultCode.OK, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<T>(ResultCode.OK, "success", null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<T>(code, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<T>(ResultCode.SYSTEM_ERROR, message, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
