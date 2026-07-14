package com.twogether.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            "요청 값이 올바르지 않습니다."
    ),

    INVALID_NICKNAME_EMPTY(
            HttpStatus.BAD_REQUEST,
            "INVALID_NICKNAME_EMPTY",
            "닉네임은 비어 있거나 공백일 수 없습니다."
    ),

    INVALID_NICKNAME_LENGTH(
            HttpStatus.BAD_REQUEST,
            "INVALID_NICKNAME_LENGTH",
            "닉네임은 2자 이상 12자 이하로 입력해주세요."
    ),

    INVALID_NICKNAME_FORMAT(
            HttpStatus.BAD_REQUEST,
            "INVALID_NICKNAME_FORMAT",
            "닉네임은 한글, 영문, 숫자, 밑줄만 사용할 수 있습니다."
    ),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER_NOT_FOUND",
            "사용자를 찾을 수 없습니다."
    ),

    DUPLICATE_NICKNAME(
            HttpStatus.CONFLICT,
            "DUPLICATE_NICKNAME",
            "이미 사용 중인 닉네임입니다."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(
            HttpStatus status,
            String code,
            String message
    ) {
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