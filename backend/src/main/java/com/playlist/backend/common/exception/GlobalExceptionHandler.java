package com.playlist.backend.common.exception;

import com.playlist.backend.common.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //  공통 응답 생성 함수 (핵심 “함수화” 부분)
    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode));
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode, String overrideMessage) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, overrideMessage));
    }

    // 1) 비즈니스 예외 (커스텀)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        // detailMessage를 사용해서 덮어쓰기
        return buildResponse(errorCode, ex.getMessage());
    }

    // 2) @Valid 유효성 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .orElse("유효성 검증에 실패했습니다.");

        return buildResponse(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    // 3) 그 외 예상치 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        // TODO: 로그 추가 (log.error 등)
        ex.printStackTrace();
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
