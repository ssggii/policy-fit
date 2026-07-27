package dev.youthpolicy.domain.atom;

/**
 * '대략(approx)' 응답이 가능한 정수 답변 입력 변수(예: 본인 소득).
 * known=false면 '모름'. approx=true면 값이 있어도 항상 확정 판정에 쓰지 않는다
 * (요구사항 확정: approx는 항상 Trilean.UNKNOWN(reason=boundary)로 처리).
 */
public record AnswerApproxInt(boolean known, boolean approx, Integer value) {
}
