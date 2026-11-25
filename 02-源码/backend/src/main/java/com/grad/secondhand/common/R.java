package com.grad.secondhand.common;

import lombok.Data;

import java.util.UUID;

/**
 * 统一响应体。需求文档 7.1：code=0 表示成功，非 0 为业务错误码
 * （4000 参数、4001 状态不允许、4003 数据范围、4009 重复、4010 登录失效、
 *   4030 无权限、4090 版本冲突、5000 服务器异常）。
 */
@Data
public class R<T> {
    private int code;
    private String message;
    private T data;
    private String requestId;

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = 0;
        r.message = "success";
        r.data = data;
        r.requestId = UUID.randomUUID().toString().substring(0, 8);
        return r;
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        r.requestId = UUID.randomUUID().toString().substring(0, 8);
        return r;
    }
}
