package com.atm.atmserver.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;      // 状态码
    private String message;    // 消息
    private T data;            // 数据
    private Long timestamp;    // 时间戳

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    // 成功响应
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    // 成功响应（带自定义消息）
    public static <T> Result<T> success(T data, String message) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    // 错误响应（默认500）
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.setCode(500);
        r.setMessage(msg);
        return r;
    }

    // 错误响应（自定义状态码）
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }

    // 参数错误
    public static <T> Result<T> badRequest(String msg) {
        Result<T> r = new Result<>();
        r.setCode(400);
        r.setMessage(msg);
        return r;
    }

    // 未授权
    public static <T> Result<T> unauthorized(String msg) {
        Result<T> r = new Result<>();
        r.setCode(401);
        r.setMessage(msg);
        return r;
    }

    // 禁止访问
    public static <T> Result<T> forbidden(String msg) {
        Result<T> r = new Result<>();
        r.setCode(403);
        r.setMessage(msg);
        return r;
    }

    // 资源不存在
    public static <T> Result<T> notFound(String msg) {
        Result<T> r = new Result<>();
        r.setCode(404);
        r.setMessage(msg);
        return r;
    }
}