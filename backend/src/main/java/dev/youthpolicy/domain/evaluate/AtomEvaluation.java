package dev.youthpolicy.domain.evaluate;

import dev.youthpolicy.domain.atom.AtomOutcome;
import dev.youthpolicy.domain.rule.RuleNode;

/** 트리의 원자 잎 하나에 대한 참조 + 평가 결과. reasoning(F-004) 구성에 쓰인다. */
public record AtomEvaluation(RuleNode.AtomRef atomRef, AtomOutcome outcome) {
}
