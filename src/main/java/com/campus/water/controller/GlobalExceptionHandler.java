package com.campus.water.controller;

import com.campus.water.util.ResultVO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * 全局异常处理器 - 统一处理项目中所有控制器层异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数格式/值错误（如枚举值非法、参数为空等）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResultVO<Void> handleIllegalArgument(IllegalArgumentException e) {
        // 针对告警级别/状态参数错误做友好提示
        String msg = e.getMessage();
        if (msg.contains("AlertLevel") || msg.contains("AlertStatus")) {
            msg = "参数错误：告警级别可选值(info/warning/error/critical)，告警状态可选值(pending/resolved/closed)";
        }
        return ResultVO.error(400, "参数错误：" + msg);
    }

    /**
     * 处理参数类型不匹配（如时间格式错误、字符串转数字失败等）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResultVO<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String errorMsg;
        // 特殊处理时间格式错误（告警查询的时间参数）
        if (e.getCause() instanceof DateTimeParseException) {
            errorMsg = "时间参数格式错误，正确格式：yyyy-MM-dd HH:mm:ss（示例：2025-12-05 10:30:00）";
        } else {
            // 通用类型不匹配提示
            errorMsg = String.format(
                    "参数[%s]类型错误，期望类型：%s，实际传入值：%s",
                    e.getName(),
                    e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知",
                    e.getValue()
            );
        }
        return ResultVO.error(400, errorMsg);
    }

    /**
     * 处理权限不足异常（如非管理员/维修人员访问告警接口）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResultVO<Void> handleAccessDenied(AccessDeniedException e) {
        return ResultVO.error(403, "权限不足：仅管理员/维修人员可访问告警相关功能");
    }

    /**
     * 处理通用运行时异常（兜底）
     */
    @ExceptionHandler(RuntimeException.class)
    public ResultVO<Void> handleRuntimeException(RuntimeException e) {
        // 生产环境建议添加日志记录，此处简化
        // log.error("服务器运行时异常", e);
        return ResultVO.error(500, "服务器内部错误：" + e.getMessage());
    }

    /**
     * 处理请求参数验证失败（如@NotBlank/@Pattern等注解验证失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        // 获取第一个验证失败的字段和消息
        String errorMsg = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return ResultVO.badRequest(errorMsg); // 返回400状态码和具体错误信息
    }
}