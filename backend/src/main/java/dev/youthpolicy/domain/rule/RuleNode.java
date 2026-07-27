package dev.youthpolicy.domain.rule;

import dev.youthpolicy.domain.atom.AtomId;

import java.util.List;
import java.util.Map;

/**
 * Rule DSL 트리 노드 — DOMAIN.md §4, contracts/rule-dsl.schema.json.
 *
 * sealed interface + record으로 4종 노드를 표현해, RuleEvaluator/OutOfScopeClassifier의
 * switch에서 분기 누락을 컴파일타임에 잡는다(ADR-0003 "합계형은 sealed + switch").
 */
public sealed interface RuleNode {

    record AllOf(List<RuleNode> children) implements RuleNode {
        public AllOf {
            if (children == null || children.isEmpty()) {
                throw new IllegalArgumentException("all_of는 최소 1개 이상의 자식 노드가 필요합니다.");
            }
            children = List.copyOf(children);
        }
    }

    record AnyOf(List<RuleNode> children) implements RuleNode {
        public AnyOf {
            if (children == null || children.isEmpty()) {
                throw new IllegalArgumentException("any_of는 최소 1개 이상의 자식 노드가 필요합니다.");
            }
            children = List.copyOf(children);
        }
    }

    record Not(RuleNode child) implements RuleNode {
        public Not {
            if (child == null) {
                throw new IllegalArgumentException("not은 자식 노드가 필요합니다.");
            }
        }
    }

    record AtomRef(AtomId atom, Map<String, Object> params, Meta meta) implements RuleNode {
        public AtomRef {
            if (atom == null) {
                throw new IllegalArgumentException("atom id는 필수입니다.");
            }
            params = params == null ? Map.of() : Map.copyOf(params);
        }
    }

    /** 출처·기준연도(SPEC F-004). */
    record Meta(String source, Integer year) {
    }
}
