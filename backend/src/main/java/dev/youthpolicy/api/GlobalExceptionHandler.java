package dev.youthpolicy.api;

import dev.youthpolicy.api.dto.ErrorResponseDto;
import dev.youthpolicy.policy.PolicyNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 미저장·무로깅 원칙(PRD 9장, ADR-0004 D2) — 요청 바디·필드값은 어떤 형태로도 로그에 남기지 않는다.
 * 예외 메시지에도 사용자가 입력한 원문 값이 실리지 않도록 고정 문구만 반환하고, 예외를 로깅하지 않는다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleMalformedBody() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("요청 본문 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("요청 값이 유효하지 않습니다."));
    }

    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handlePolicyNotFound() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("등록되지 않은 policy_id 입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("알 수 없는 오류가 발생했습니다."));
    }
}
