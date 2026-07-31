package dev.youthpolicy.domain.evaluate;

import dev.youthpolicy.domain.kleene.Trilean;

import java.util.List;

/**
 * 트리 전체 조합 결과(Trilean) + 모든 원자 잎의 개별 평가(reasoning 구성용)
 * + 그 결과를 실제로 결정한(흡수되지 않은) 원자 평가(unknown_reasons 구성용, DOMAIN §3.5).
 */
public record RuleEvaluationResult(Trilean value, List<AtomEvaluation> atomEvaluations,
                                    List<AtomEvaluation> contributingEvaluations) {

    public RuleEvaluationResult {
        atomEvaluations = List.copyOf(atomEvaluations);
        contributingEvaluations = List.copyOf(contributingEvaluations);
    }
}
