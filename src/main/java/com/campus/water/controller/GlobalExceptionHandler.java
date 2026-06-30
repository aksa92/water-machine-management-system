package com.campus.water.controller;

import com.campus.water.entity.BusinessException;
import com.campus.water.util.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器 - 统一处理项目中所有控制器层异常
 */
@RestControllerAdvice
@Slf4j
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
        // 设备ID格式错误特殊处理
        if (msg.contains("设备ID") || msg.contains("deviceId")) {
            msg = "设备ID格式错误，正确格式为WM/WS开头+3位数字（如WM001、WS123）";
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
        if (e.getCause() instanceof java.time.format.DateTimeParseException) {
            errorMsg = "时间参数格式错误";
        } else if (e.getRequiredType() != null && e.getRequiredType().isEnum()) {
            // 枚举类型转换错误处理
            errorMsg = String.format(
                    "参数枚举值错误",
                    e.getName(),
                    getEnumValues(e.getRequiredType())
            );
        } else {
            // 通用类型不匹配提示
            errorMsg = String.format(
                    "参数类型错误",
                    e.getName(),
                    e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知",
                    e.getValue()
            );
        }
        return ResultVO.error(400, errorMsg);
    }

    /**
     * 处理权限不足异常（如非管理员/维修人员访问受限接口）
     */
    @ExceptionHandler({AccessDeniedException.class, org.springframework.security.access.AccessDeniedException.class})
    public ResultVO<Void> handleAccessDenied(Exception e) {
        String roleMsg = "仅超级管理员可操作";
        // 区分不同接口的权限提示
        if (e.getMessage().contains("AREA_ADMIN")) {
            roleMsg = "仅区域管理员及以上权限可操作";
        } else if (e.getMessage().contains("REPAIRMAN")) {
            roleMsg = "仅维修人员及管理员可操作";
        }
        return ResultVO.error(403, "权限不足：" + roleMsg);
    }

    /**
     * 处理资源不存在异常（如查询不存在的设备/用户）
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResultVO<Void> handleNoSuchElement(NoSuchElementException e) {
        String msg = e.getMessage();
        // 标准化资源不存在提示
        if (msg.contains("设备")) {
            return ResultVO.error(404, "设备不存在：" + msg.replace("No value present", "").trim());
        } else if (msg.contains("管理员") || msg.contains("Admin")) {
            return ResultVO.error(404, "管理员不存在：" + msg.replace("No value present", "").trim());
        } else if (msg.contains("区域") || msg.contains("Area")) {
            return ResultVO.error(404, "区域不存在：" + msg.replace("No value present", "").trim());
        }
        return ResultVO.error(404, "请求的资源不存在：" + msg);
    }

    /**
     * 处理请求参数验证失败（如@NotBlank/@Pattern等注解验证失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        // 收集所有验证失败的字段和消息
        List<String> errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + "：" + error.getDefaultMessage())
                .collect(Collectors.toList());
        return ResultVO.error(400, "参数验证失败：" + String.join("；", errorMessages));
    }

    /**
     * 处理表单参数绑定异常（非@RequestBody的参数验证）
     */
    @ExceptionHandler(BindException.class)
    public ResultVO<Void> handleBindException(BindException e) {
        FieldError firstError = e.getBindingResult().getFieldError();
        String errorMsg = firstError != null ?
                firstError.getField() + "：" + firstError.getDefaultMessage() :
                "参数绑定失败";
        return ResultVO.error(400, "表单参数错误：" + errorMsg);
    }

    /**
     * 处理缺失必填参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResultVO<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return ResultVO.error(400,
                String.format("缺少必填参数：%s（类型：%s）",
                        e.getParameterName(),
                        e.getParameterType()));
    }

    /**
     * 处理数据库唯一约束冲突异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResultVO<Void> handleDuplicateKey(DuplicateKeyException e) {
        log.error("数据库唯一约束冲突", e);
        String msg = "数据已存在，无法重复添加";
        // 针对设备ID冲突特殊处理
        if (e.getMessage().contains("device_id")) {
            msg = "设备ID已存在，请更换设备ID后重试";
        } else if (e.getMessage().contains("admin_name")) {
            msg = "管理员用户名已存在，请更换用户名";
        }
        return ResultVO.error(409, msg);
    }

    /**
     * 处理数据库完整性约束异常（外键关联等）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResultVO<Void> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("数据库完整性约束异常", e);
        String msg = "数据操作失败，可能存在关联数据";
        if (e.getMessage().contains("foreign key constraint")) {
            msg = "无法删除，该数据已被其他记录关联引用";
        } else if (e.getMessage().contains("not null")) {
            msg = "必填字段不能为空，请检查输入";
        }
        return ResultVO.error(400, msg);
    }

    /**
     * 处理JSON解析异常（请求体格式错误）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResultVO<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("请求体解析失败", e);
        String msg = "请求数据格式错误，请检查JSON格式是否正确";
        if (e.getMessage().contains("date-time")) {
            msg = "日期时间格式错误，正确格式：yyyy-MM-dd HH:mm:ss";
        }
        return ResultVO.error(400, msg);
    }

    /**
     * 处理不支持的HTTP方法异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResultVO<Void> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResultVO.error(405,
                String.format("不支持的请求方法：%s，支持的方法：%s",
                        e.getMethod(),
                        String.join(",", e.getSupportedMethods())));
    }

    /**
     * 处理不支持的媒体类型异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResultVO<Void> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return ResultVO.error(415,
                String.format("不支持的媒体类型：%s，支持的类型：%s",
                        e.getContentType(),
                        e.getSupportedMediaTypes()));
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResultVO<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        long maxSizeMB = e.getMaxUploadSize() / (1024 * 1024);
        return ResultVO.error(413,
                String.format("文件大小超限，最大支持：%dMB", maxSizeMB));
    }

    /**
     * 处理IO异常（文件操作等）
     */
    @ExceptionHandler(IOException.class)
    public ResultVO<Void> handleIOException(IOException e) {
        log.error("IO操作异常", e);
        String msg = "文件操作失败：" + e.getMessage();
        if (e.getMessage().contains("Permission denied")) {
            msg = "文件操作权限不足";
        }
        return ResultVO.error(500, msg);
    }

    /**
     * 处理业务逻辑异常（自定义异常）
     */
    @ExceptionHandler(BusinessException.class)
    public ResultVO<Void> handleBusinessException(BusinessException e) {
        // 业务异常自带状态码和消息
        return ResultVO.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResultVO<Void> handleNoHandlerFound(NoHandlerFoundException e) {
        return ResultVO.error(404,
                String.format("请求的接口不存在：%s %s",
                        e.getHttpMethod(),
                        e.getRequestURL()));
    }

    /**
     * 处理通用运行时异常（兜底）
     */
    @ExceptionHandler(RuntimeException.class)
    public ResultVO<Void> handleRuntimeException(RuntimeException e) {
        log.error("服务器运行时异常", e);
        // 生产环境可根据异常类型返回更友好的提示
        String msg = "服务器内部错误：" + e.getMessage();
        // 对常见运行时异常进行特殊处理
        if (e instanceof NullPointerException) {
            msg = "系统处理异常：数据为空";
        } else if (e instanceof IndexOutOfBoundsException) {
            msg = "系统处理异常：数据索引越界";
        }
        return ResultVO.error(500, msg);
    }

    /**
     * 工具方法：获取枚举类的所有值
     */
    private String getEnumValues(Class<?> enumClass) {
        if (!enumClass.isEnum()) {
            return "未知";
        }
        StringBuilder values = new StringBuilder();
        for (Object enumConstant : enumClass.getEnumConstants()) {
            values.append(enumConstant).append(",");
        }
        if (values.length() > 0) {
            values.deleteCharAt(values.length() - 1);
        }
        return values.toString();
    }
}