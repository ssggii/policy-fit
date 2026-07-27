package dev.youthpolicy.domain.scope;

import dev.youthpolicy.domain.atom.AtomCatalog;
import dev.youthpolicy.domain.atom.ValueType;
import dev.youthpolicy.domain.rule.RuleNode;

/**
 * DOMAIN §1.2·§2.2·§3.4, ADR-0003 D3 — household_aggregate 원자를 참조하는 정책은
 * 런타임 평가 이전에 out_of_scope로 사전분류한다. out_of_scope는 계산 결과가 아니라
 * 규칙의 정적 속성이다.
 *
 * 원자 카탈로그의 값 유형 매핑만 참조하므로, 해당 원자에 evaluator가 등록돼 있는지와
 * 무관하게 분류가 성립한다(엔진이 원자 카탈로그 완결성에 의존하지 않음을 보장).
 */
public final class OutOfScopeClassifier {

    public boolean isOutOfScope(RuleNode node) {
        return switch (node) {
            case RuleNode.AllOf allOf -> allOf.children().stream().anyMatch(this::isOutOfScope);
            case RuleNode.AnyOf anyOf -> anyOf.children().stream().anyMatch(this::isOutOfScope);
            case RuleNode.Not not -> isOutOfScope(not.child());
            case RuleNode.AtomRef atomRef -> AtomCatalog.valueTypeOf(atomRef.atom()) == ValueType.HOUSEHOLD_AGGREGATE;
        };
    }
}
