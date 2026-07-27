package dev.youthpolicy.domain.atom;

import java.util.Map;

/**
 * 재사용 가능한 원자 평가기 — DOMAIN.md §2.1. 원자는 정책을 모른다.
 * 정책 규칙(Rule DSL)이 params를 주입해 재사용한다.
 */
public interface AtomEvaluator {

    AtomOutcome evaluate(Map<String, Object> params, Answers answers);
}
