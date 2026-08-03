package dev.youthpolicy.policy;

import dev.youthpolicy.domain.rule.RuleNode;

/**
 * 정책 레코드 — DOMAIN §4.4: rule(Rule DSL 트리) + 형제 필드(id·name·application).
 * 정책 추가 = 코드가 아니라 이런 레코드 데이터 한 건(ADR-0003 D1).
 *
 * <p>{@code outOfScopeGate}는 선택적 범위 게이트(ADR-0005 D5) — 자격 rule과 형제인 별도 Rule DSL
 * 트리로, 자가판정 가능 여부를 가른다. 게이트가 없는 정책은 {@code null}이다.
 */
public record PolicyRecord(String id, String name, RuleNode rule, RuleNode outOfScopeGate,
                            PolicyApplication application) {
}
