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

    GATHERING_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "GATHERING_NOT_FOUND",
            "모임을 찾을 수 없습니다."
    ),

    GATHERING_NOT_HOST(
            HttpStatus.FORBIDDEN,
            "GATHERING_NOT_HOST",
            "방장만 수행할 수 있는 작업입니다."
    ),

    GATHERING_NOT_RECRUITING(
            HttpStatus.CONFLICT,
            "GATHERING_NOT_RECRUITING",
            "모집 중인 모임에서만 수행할 수 있습니다."
    ),

    GATHERING_FULL(
            HttpStatus.CONFLICT,
            "GATHERING_FULL",
            "모집 인원이 마감된 모임입니다."
    ),

    INVALID_GATHERING_I