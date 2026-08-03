package dev.youthpolicy.domain.evaluate;

import dev.youthpolicy.domain.verdict.Verdict;

import java.util.List;

/**
 * 한 정책에 대한 최종 판정 결과 — {@link PolicyEvaluator}의 산출물.
 *
 * @param verdict   4-state 판정
 * @param reasoning 자격 요건(base rule) 원자별 평가 — reasoning(F-004) 구성용. out_of_scope는 빈 리스트
 *                  (범위 밖 정책은 자격 요건을 평가하지 않는다 — 정적·런타임 out_of_scope 공통).
 */
public record PolicyEvaluation(Verdict verdict, List<AtomEvaluation> reasoning) {

    public PolicyEvaluation {
        reasoning = List.copyOf(reasoning);
    }
}
