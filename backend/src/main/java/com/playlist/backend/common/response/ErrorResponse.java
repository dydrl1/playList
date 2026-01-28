package com.playlist.backend.common.response;

import com.playlist.backend.common.exception.ErrorCode;
import java.util.List;

public class ErrorResponse {

    private final String code;
    private final String message;
    private final List<FieldErrorDetail> fieldErrors;

    public ErrorResponse(String code, String message) {
        this(code, message, List.of());
    }

    public ErrorResponse(String code, String message, List<FieldErrorDetail> fieldErrors) {
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors == null ? List.of() : fieldErrors;
    }

    // ErrorCode => ErrorResponse
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    // 메시지를 덮어쓰고 싶을 때
    public static ErrorResponse of(ErrorCode errorCode, String overrideMessage) {
        return new ErrorResponse(errorCode.getCode(), overrideMessage);
    }

    // DTO 검증 실패 등: fieldErrors까지 포함
    public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), fieldErrors);
    }

    // DTO 검증 실패 등: message + fieldErrors 모두 커스텀
    public static ErrorResponse of(ErrorCode errorCode, String overrideMessage, List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(errorCode.getCode(), overrideMessage, fieldErrors);
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public List<FieldErrorDetail> getFieldErrors() { return fieldErrors; }

    public static class FieldErrorDetail {
        private final String field;
        private final String message;

        public FieldErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public String getMessage() { return message; }
    }
}
