package com.playlist.backend.common.response;

import com.playlist.backend.common.exception.ErrorCode;

public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorResponse error;

    private ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<?> failure(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, ErrorResponse.from(errorCode));
    }

    public static ApiResponse<?> failure(ErrorCode errorCode, String overrideMessage) {
        return new ApiResponse<>(false, null, ErrorResponse.of(errorCode, overrideMessage));
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ErrorResponse getError() { return error; }
}
