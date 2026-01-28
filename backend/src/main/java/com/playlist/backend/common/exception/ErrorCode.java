package com.playlist.backend.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {


    /* =======================
       400 BAD REQUEST (요청 오류)
    ======================= */
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청 값입니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "C002", "이메일 형식이 올바르지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C003", "잘못된 요청입니다."),

    PLAYLIST_TRACK_ORDER_INVALID(HttpStatus.BAD_REQUEST, "PT001", "유효하지 않은 트랙 순서 값입니다."),
    PLAYLIST_TRACK_ORDER_MISMATCH(HttpStatus.BAD_REQUEST, "PT002", "요청된 트랙 순서가 현재 플레이리스트 구성과 일치하지 않습니다."),


    /* =======================
       401 UNAUTHORIZED (인증)
    ======================= */
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A001", "인증에 실패했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A004", "토큰이 만료되었습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "A005", "리프레시 토큰이 유효하지 않습니다."),


    /* =======================
       403 FORBIDDEN (권한)
    ======================= */
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A003", "접근 권한이 없습니다."),
    PLAYLIST_PRIVATE(HttpStatus.FORBIDDEN, "P002", "비공개 플레이리스트입니다."),
    PLAYLIST_MODIFY_FORBIDDEN(HttpStatus.FORBIDDEN, "P003", "플레이리스트 수정 권한이 없습니다."),
    PLAYLIST_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "P004", "플레이리스트 삭제 권한이 없습니다."),



    /* =======================
       404 NOT FOUND (리소스 없음)
    ======================= */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 사용자입니다."),
    PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "존재하지 않는 플레이리스트입니다."),
    TRACK_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "존재하지 않는 트랙입니다."),
    PLAYLIST_TRACK_NOT_FOUND(HttpStatus.NOT_FOUND, "PT003", "존재하지 않는 플레이리스트 트랙입니다."),


    /* =======================
       409 CONFLICT (중복/충돌)
    ======================= */
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "U002", "이미 가입된 이메일입니다."),
    PLAYLIST_TRACK_ALREADY_EXISTS(HttpStatus.CONFLICT, "PT004", "플레이리스트에 이미 존재하는 트랙입니다."),
    TRACK_SEARCH_KEYWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "T002", "검색어는 최소 2자 이상이어야 합니다."),
    TRACK_SOURCE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "T003", "지원하지 않는 트랙 소스입니다."),


    /* =======================
       429 TOO MANY REQUESTS (요청 제한)
    ======================= */
    YOUTUBE_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "YOUTUBE_429", "현재 유튜브 검색 요청이 많아 잠시 후 다시 시도해 주세요."),


    /* =======================
        502 / 504 EXTERNAL API ERROR
    ======================= */
    EXTERNAL_API_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "S005", "외부 서비스 응답이 올바르지 않습니다."),
    EXTERNAL_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "S004", "외부 서비스 응답이 지연되고 있습니다."),



    /* =======================
       500 INTERNAL SERVER ERROR (서버)
    ======================= */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 내부 오류입니다."),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "외부 서비스 연동 중 오류가 발생했습니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
