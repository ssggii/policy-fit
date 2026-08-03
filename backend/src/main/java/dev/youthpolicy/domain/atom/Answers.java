package dev.youthpolicy.domain.atom;

/**
 * 사용자 입력 변수 묶음(도메인 표현). API 요청의 answers 객체를 그대로 옮긴 것이며,
 * "입력 변수 ≠ 원자"(DOMAIN §2.1) 원칙에 따라 원자 ID가 아니라 질문 변수명으로 필드를 둔다.
 * Phase 7 슬라이스는 청년 주택드림 청약통장·청년전용 버팀목 전세자금대출·청년월세 특별지원 정책의 변수를 다룬다.
 * {@code married}는 청년월세 면제 게이트(ADR-0005 D5)의 household_composition{married} 경로에 쓰인다.
 */
public record Answers(
        AnswerInt age,
        AnswerBool housingNone,
        AnswerApproxInt incomeSelfMonthlyKrw,
        AnswerString leaseType,
        AnswerApproxInt assetSelfKrw,
        AnswerBool married) {
}
