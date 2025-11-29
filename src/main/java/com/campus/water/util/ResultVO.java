package com.campus.water.util;

import lombok.Data;

@Data
public class ResultVO<T> {
    private Integer code;    // 状态码
    private String message;  // 提示信息
    private T data;          // 返回数据
    private Long timestamp;  // 时间戳

    public ResultVO() {
        this.timestamp = System.currentTimeMillis();
    }

    // 成功返回（带数据）
    public static <T> ResultVO<T> success(T data) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    // 成功返回（带数据和自定义消息）
    public static <T> ResultVO<T> success(T data, String message) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // 成功返回（无数据）
    public static <T> ResultVO<T> success() {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    // 错误返回
    public static <T> ResultVO<T> error(Integer code, String message) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    // 常见的错误状态
    public static <T> ResultVO<T> error(String message) {
        return error(500, message);
    }

    public static <T> ResultVO<T> badRequest(String message) {
        return error(400, message);
    }

    public static <T> ResultVO<T> unauthorized(String message) {
        return error(401, message);
    }

    public static <T> ResultVO<T> notFound(String message) {
        return error(404, message);
    }
}