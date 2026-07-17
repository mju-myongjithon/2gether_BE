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

    INVALID_AVAILABILITY_TIME_ORDER(
            HttpStatus.BAD_REQUEST,
            "INVALID_AVAILABILITY_TIME_ORDER",
            "시작 시간은 종료 시간보다 빨라야 합니다."
    ),

    INVALID_AVAILABILITY_TIME_UNIT(
            HttpStatus.BAD_REQUEST,
            "INVALID_AVAILABILITY_TIME_UNIT",
            "가용 일정은 30분 단위로 입력해야 합니다."
    ),

    DUPLICATE_AVAILABILITY(
            HttpStatus.BAD_REQUEST,
            "DUPLICATE_AVAILABILITY",
            "중복된 가용 일정이 포함되어 있습니다."
    ),

    INVALID_TAG(
            HttpStatus.BAD_REQUEST,
            "INVALID_TAG",
            "존재하지 않는 태그가 포함되어 있습니다."
    ),

    GATHERING_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "GATHERING_NOT_FOUND",
            "모임을 찾을 수 없습니다."
    ),

    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "FORBIDDEN",
            "권한이 없습니다."
    ),

    GATHERING_NOT_MODIFIABLE(
            HttpStatus.CONFLICT,
            "GATHERING_NOT_MODIFIABLE",
            "모집 중인 모임만 수정/취소할 수 있습니다."
    ),

    GATHERING_COMPLETE_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "GATHERING_COMPLETE_FORBIDDEN",
            "모임장만 모임을 완료할 수 있습니다."
    ),

    INVALID_GATHERING_STATUS_FOR_COMPLETE(
            HttpStatus.CONFLICT,
            "INVALID_GATHERING_STATUS_FOR_COMPLETE",
            "확정된 모임만 완료할 수 있습니다."
    ),

    VERIFICATION_NOT_COMPLETED_GATHERING(
            HttpStatus.CONFLICT,
            "VERIFICATION_NOT_COMPLETED_GATHERING",
            "완료된 모임만 활동 인증을 제출할 수 있습니다."
    ),

    VERIFICATION_SUBMIT_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "VERIFICATION_SUBMIT_FORBIDDEN",
            "모임 참여자만 활동 인증을 제출할 수 있습니다."
    ),

    VERIFICATION_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "VERIFICATION_ALREADY_EXISTS",
            "해당 모임의 활동 인증이 이미 존재합니다."
    ),

    VERIFICATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "VERIFICATION_NOT_FOUND",
            "활동 인증을 찾을 수 없습니다."
    ),

    VERIFICATION_EVALUATE_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "VERIFICATION_EVALUATE_FORBIDDEN",
            "모임 참여자만 활동 인증을 판정할 수 있습니다."
    ),

    VERIFICATION_ALREADY_EVALUATED(
            HttpStatus.CONFLICT,
            "VERIFICATION_ALREADY_EVALUATED",
            "이미 판정이 완료된 활동 인증입니다."
    ),

    INVALID_AI_VERIFICATION_RESULT(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INVALID_AI_VERIFICATION_RESULT",
            "AI 활동 인증 판정 결과가 올바르지 않습니다."
    ),

    GATHERING_NOT_RECRUITING(
            HttpStatus.CONFLICT,
            "GATHERING_NOT_RECRUITING",
            "모집이 마감된 모임입니다."
    ),

    DUPLICATE_APPLICATION(
            HttpStatus.CONFLICT,
            "DUPLICATE_APPLICATION",
            "이미 신청한 모임입니다."
    ),

    ALREADY_MEMBER(
            HttpStatus.CONFLICT,
            "ALREADY_MEMBER",
            "이미 참여 중인 모임입니다."
    ),

    APPLICATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "APPLICATION_NOT_FOUND",
            "신청을 찾을 수 없습니다."
    ),

    GATHERING_MEMBER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "GATHERING_MEMBER_NOT_FOUND",
            "모임 멤버를 찾을 수 없습니다."
    ),

    APPLICATION_ALREADY_PROCESSED(
            HttpStatus.CONFLICT,
            "APPLICATION_ALREADY_PROCESSED",
            "이미 처리된 신청입니다."
    ),

    CAPACITY_EXCEEDED(
            HttpStatus.CONFLICT,
            "CAPACITY_EXCEEDED",
            "모집 정원을 초과했습니다."
    ),

    INVALID_TAG_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_TAG_REQUEST",
            "태그 목록은 null일 수 없습니다."
    ),

    TAG_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "TAG_NOT_FOUND",
            "요청한 태그를 찾을 수 없습니다."
    ),

    INVALID_TAG_TYPE(
            HttpStatus.BAD_REQUEST,
            "INVALID_TAG_TYPE",
            "요청한 태그 유형이 올바르지 않습니다."
    ),

    NOTICE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "NOTICE_NOT_FOUND",
            "공지사항을 찾을 수 없습니다."
    ),

    COMMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "COMMENT_NOT_FOUND",
            "댓글을 찾을 수 없습니다."
    ),
  
    RECOMMENDATION_GATHERING_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "RECOMMENDATION_GATHERING_MISMATCH",
            "경로의 모임 ID와 요청 본문의 모임 ID가 일치하지 않습니다."
    ),

    RECOMMENDATION_CANDIDATE_EMPTY(
            HttpStatus.BAD_REQUEST,
            "RECOMMENDATION_CANDIDATE_EMPTY",
            "추천 가능한 후보 사용자가 없습니다."
    ),

    RECOMMENDATION_CANDIDATE_LIMIT_EXCEEDED(
            HttpStatus.BAD_REQUEST,
            "RECOMMENDATION_CANDIDATE_LIMIT_EXCEEDED",
            "AI 추천 후보는 최대 20명까지 전달할 수 있습니다."
    ),

    INVALID_RECOMMENDATION_COUNT(
            HttpStatus.BAD_REQUEST,
            "INVALID_RECOMMENDATION_COUNT",
            "추천 인원은 1명 이상이며 후보 사용자 수보다 많을 수 없습니다."
    ),
  
    CHAT_ROOM_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CHAT_ROOM_NOT_FOUND",
            "채팅방을 찾을 수 없습니다."
    ),

    CHAT_ROOM_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "CHAT_ROOM_ACCESS_DENIED",
            "채팅방에 접근할 권한이 없습니다."
    ),

    TOPIC_RECOMMENDATION_NOT_AVAILABLE(
            HttpStatus.BAD_REQUEST,
            "TOPIC_RECOMMENDATION_NOT_AVAILABLE",
            "모임과 연결된 채팅방에서만 대화 주제를 추천할 수 있습니다."
    ),

    AI_RECOMMENDATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "AI_RECOMMENDATION_FAILED",
            "대화 주제 추천 결과를 생성하지 못했습니다."
    ),

    CHAT_NOTICE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CHAT_NOTICE_NOT_FOUND",
            "활성화된 공지가 없습니다."
    ),

    INVALID_QUICK_CODE(
            HttpStatus.BAD_REQUEST,
            "INVALID_QUICK_CODE",
            "유효하지 않은 안심 커넥트 코드입니다."
    ),

    EXPIRED_QUICK_CODE(
            HttpStatus.BAD_REQUEST,
            "EXPIRED_QUICK_CODE",
            "만료된 안심 커넥트 코드입니다."
    ),

    ALREADY_USED_QUICK_CODE(
            HttpStatus.CONFLICT,
            "ALREADY_USED_QUICK_CODE",
            "이미 사용된 안심 커넥트 코드입니다."
    ),

    NOTIFICATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "NOTIFICATION_NOT_FOUND",
            "알림을 찾을 수 없습니다."
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
