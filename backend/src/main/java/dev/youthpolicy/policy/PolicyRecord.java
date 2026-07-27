package dev.youthpolicy.policy;

import dev.youthpolicy.domain.rule.RuleNode;

/**
 * 정책 레코드 — DOMAIN §4.4: rule(Rule DSL 트리) + 형제 필드(id·name·application).
 * 정책 추가 = 코드가 아니라 이런 레코드 데이터 한 건(ADR-0003 D1).
 */
public record PolicyRecord(String id, String name, RuleNode rule, PolicyApplication application) {
}
