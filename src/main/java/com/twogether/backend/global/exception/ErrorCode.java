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

    INVALID_INTRODUCTION_LENGTH(
            HttpStatus.BAD_REQUEST,
            "INVALID_INTRODUCTION_LENGTH",
            "자기소개는 200자 이하로 입력해주세요."
    ),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER_NOT_FOUND",
            "사용자를 찾을 수 없습니다."
    ),

    DEPARTMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "DEPARTMENT_NOT_FOUND",
            "선택한 학과를 찾을 수 없습니다."
    ),

    DUPLICATE_NICKNAME(
            HttpStatus.CONFLICT,
            "DUPLICATE_NICKNAME",
            "이미 사용 중인 닉네임입니다."
    ),

    INVALID_SCHOOL_EMAIL(
            HttpStatus.BAD_REQUEST,
            "INVALID_SCHOOL_EMAIL",
            "명지대학교 이메일 형식이 아닙니다."
    ),

    EMAIL_ALREADY_VERIFIED(
            HttpStatus.CONFLICT,
            "EMAIL_ALREADY_VERIFIED",
            "이미 학교 이메일 인증이 완료된 사용자입니다."
    ),

    SCHOOL_EMAIL_ALREADY_IN_USE(
            HttpStatus.CONFLICT,
            "SCHOOL_EMAIL_ALREADY_IN_USE",
            "이미 다른 사용자가 인증한 학교 이메일입니다."
    ),

    VERIFICATION_CODE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "VERIFICATION_CODE_NOT_FOUND",
            "발급된 인증번호를 찾을 수 없습니다."
    ),

    INVALID_VERIFICATION_CODE(
            HttpStatus.BAD_REQUEST,
            "INVALID_VERIFICATION_CODE",
            "인증번호가 일치하지 않습니다."
    ),

    EXPIRED_VERIFICATION_CODE(
            HttpStatus.BAD_REQUEST,
            "EXPIRED_VERIFICATION_CODE",
            "인증번호가 만료되었습니다."
    ),

    ALREADY_USED_VERIFICATION_CODE(
            HttpStatus.CONFLICT,
            "ALREADY_USED_VERIFICATION_CODE",
            "이미 사용된 인증번호입니다."
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