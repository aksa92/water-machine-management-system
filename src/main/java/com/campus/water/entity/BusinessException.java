package com.campus.water.entity; // 请确保这个包名与你的项目结构一致

import org.springframework.http.HttpStatus;

/**
 * 自定义业务异常
 * <p>
 * 用于封装业务层抛出的、需要明确反馈给用户的异常信息。
 * 例如：用户不存在、密码错误、工单状态不正确等。
 * </p>
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码，对应HTTP状态码
     */
    private int code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 构造函数
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数，默认使用 400 Bad Request 状态码
     * @param message 错误消息
     */
    public BusinessException(String message) {
        this(HttpStatus.BAD_REQUEST.value(), message);
    }

    // --- Getters ---

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}