package dev.youthpolicy.domain.atom;

/**
 * 원자의 값 유형 — DOMAIN.md §2.2. '부적합'과 'out_of_scope'를 가르고 unknown 전파를 좌우한다.
 *
 * - SELF: 본인이 답할 수 있음 → 정상 3치 평가(모름 → unknown).
 * - HOUSEHOLD_AGGREGATE: 가구·원가구·부부 합산이 필요 → 이 원자를 요구하는 정책은
 *   런타임 평가 이전에 out_of_scope로 사전분류된다(ADR-0003 D3).
 * - ADMIN_DISCRETION: 행정청 재량 판단 → 항상 unknown 반환(§3.4), any_of 대체 경로로 해소 가능.
 */
public enum ValueType {
    SELF,
    HOUSEHOLD_AGGREGATE,
    ADMIN_DISCRETION
}
