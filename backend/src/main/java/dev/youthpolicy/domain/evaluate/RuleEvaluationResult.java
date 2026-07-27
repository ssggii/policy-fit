package dev.youthpolicy.domain.evaluate;

import dev.youthpolicy.domain.kleene.Trilean;

import java.util.List;

/** 트리 전체 조합 결과(Trilean) + 모든 원자 잎의 개별 평가(reasoning 구성용). */
public record RuleEvaluationResult(Trilean value, List<AtomEvaluation> atomEvaluations) {

    public RuleEvaluationResult {
        atomEvaluations = List.copyOf(atomEvaluations);
    }
}
