package com.twogether.backend.global.test;

import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Exception Test API",
        description = "공통 예외 처리 동작 확인용 API"
)
@RestController
public class ExceptionTestController {

    @Operation(
            summary = "비즈니스 예외 테스트",
            description = """
                    DUPLICATE_NICKNAME 비즈니스 예외를 발생시킵니다.
                    GlobalExceptionHandler가 예외를 처리하고 HTTP 409 응답을 반환합니다.
                    """
    )
    @GetMapping("/api/test/business-error")
    public void testBusinessException() {
        throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
    }

    @Operation(
            summary = "예상하지 못한 예외 테스트",
            description = """
                    예상하지 못한 일반 예외를 발생시킵니다.
                    프론트에는 안전한 공통 메시지만 반환하고,
                    실제 예외 정보는 백엔드 로그에 기록합니다.
                    """
    )
    @GetMapping("/api/test/unexpected-error")
    public void testUnexpectedException() {
        throw new IllegalStateException("테스트용 예상하지 못한 예외");
    }
}