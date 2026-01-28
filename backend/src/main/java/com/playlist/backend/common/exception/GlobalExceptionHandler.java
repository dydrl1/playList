package com.playlist.backend.common.exception;

import com.playlist.backend.common.response.ErrorResponse;
import com.playlist.backend.integration.exception.ExternalApiQuotaExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    // fieldErrors 포함 버전 추가
    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode, List<ErrorResponse.FieldErrorDetail> fieldErrors) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, fieldErrors));
    }

    // 1) 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        // 너가 detailMessage로 덮어쓰는 전략 유지
        return buildResponse(errorCode, ex.getMessage());
    }

    // 2) @Valid 유효성 검증 실패 (DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new ErrorResponse.FieldErrorDetail(
                        err.getField(),
                        err.getDefaultMessage()
                ))
                .toList();

        // 공통 코드/메시지는 INVALID_INPUT_VALUE로 통일 + 상세는 fieldErrors
        return buildResponse(ErrorCode.INVALID_INPUT_VALUE, fieldErrors);
    }

    // (추천) JSON 파싱 실패/타입 불일치도 입력값 오류로 통일
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        return buildResponse(ErrorCode.INVALID_INPUT_VALUE);
    }

    // 3) 그 외 예상치 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected exception occurred", ex);
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    // 4) 유튜브 쿼터
    @ExceptionHandler(ExternalApiQuotaExceededException.class)
    public ResponseEntity<ErrorResponse> handleYoutubeQuota(ExternalApiQuotaExceededException e) {
        return buildResponse(ErrorCode.YOUTUBE_QUOTA_EXCEEDED);
    }
}


