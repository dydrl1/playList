package com.playlist.backend.common.response;

import com.playlist.backend.common.exception.ErrorCode;

public class ErrorResponse {

    private final String code;
    private final String message;

    public ErrorResponse(String code, String message){
        this.code = code;
        this.message = message;
    }

    // ErrorCdoe => ErrorResponse 변환 "함수화"
    public static ErrorResponse from(ErrorCode errorCode){
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    // 메시지를 덮어쓰고 싶을 때
    public static ErrorResponse of(ErrorCode errorCode, String overrideMessage){
        return new ErrorResponse(errorCode.getCode(), overrideMessage);
    }

    public String getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }
}
