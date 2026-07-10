package com.twogether.backend.global.test;

import com.twogether.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Test API", description = "공통 응답 테스트 API")
@RestController
public class TestResponseController {

    @Operation(
            summary = "공통 응답 테스트",
            description = "ApiResponse 공통 응답 형식이 정상적으로 반환되는지 확인합니다."
    )
    @GetMapping("/api/test/response")
    public ResponseEntity<ApiResponse<Map<String, String>>> testResponse() {
        Map<String, String> data = Map.of(
                "name", "유대감",
                "status", "OK"
        );

        return ResponseEntity.ok(
                ApiResponse.success("공통 응답 테스트 성공", data)
        );
    }
}