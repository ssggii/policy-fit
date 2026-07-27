package dev.youthpolicy.domain.atom;

/**
 * 사용자 입력 변수 묶음(도메인 표현). API 요청의 answers 객체를 그대로 옮긴 것이며,
 * "입력 변수 ≠ 원자"(DOMAIN §2.1) 원칙에 따라 원자 ID가 아니라 질문 변수명으로 필드를 둔다.
 * Phase 7 슬라이스는 청년 주택드림 청약통장 1개 정책의 변수만 다룬다.
 */
public record Answers(AnswerInt age, AnswerBool housingNone, AnswerApproxInt incomeSelfMonthlyKrw) {
}
