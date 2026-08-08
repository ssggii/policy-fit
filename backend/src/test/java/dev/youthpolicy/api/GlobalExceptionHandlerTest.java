package dev.youthpolicy.api;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GlobalExceptionHandler의 일반 Exception 500 폴백 경로 테스트.
 * 처리기가 등록되지 않은 예외가 던져지면 500 + 고정 메시지를 반환하고, 예외 원문(입력값이
 * 섞일 수 있는)이 응답에 새지 않아야 한다(미저장·무로깅 원칙, PRD 9장).
 */
class GlobalExceptionHandlerTest {

    /** 처리기가 없는 임의의 런타임 예외를 던지는 테스트 전용 컨트롤러. */
    @RestController
    static class ThrowingController {
        @GetMapping("/_test/boom")
        String boom() {
            throw new IllegalStateException("사용자 입력 원문 3000000 — 응답에 새면 안 됨");
        }
    }

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void unexpectedException_returns500WithFixedMessage() throws Exception {
        mockMvc.perform(get("/_test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("알 수 없는 오류가 발생했습니다."));
    }

    @Test
    void unexpectedException_doesNotLeakExceptionDetail() throws Exception {
        mockMvc.perform(get("/_test/boom"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("3000000"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("IllegalState"))));
    }
}
