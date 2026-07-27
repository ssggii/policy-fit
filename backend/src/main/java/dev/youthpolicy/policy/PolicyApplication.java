package dev.youthpolicy.policy;

/**
 * DOMAIN §4.4 — 신청 채널·선발 방식·시기(F-006·F-007). Rule DSL 밖의 형제 필드.
 *
 * <p>TODO(Phase 7 공식 검증 필요): {@code resources/policies/*.json}의 이 필드 값들은 rule의
 * atom과 달리 {@code meta.source}/{@code year} 같은 출처 표식을 실을 자리가 없다(계약에 없음).
 * 실사용자에게 노출되는 신청 링크·선발 방식·시기이므로, 정책 JSON 작성 시 실제로 공식 출처로
 * 검증됐는지 확인하고, 아직이면 노출 전에 검증부터 완료할 것.
 */
public record PolicyApplication(String url, String selectionMethod, String period) {
}
