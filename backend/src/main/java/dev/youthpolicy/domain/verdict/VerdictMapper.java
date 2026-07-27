package dev.youthpolicy.domain.verdict;

import dev.youthpolicy.domain.evaluate.AtomEvaluation;
import dev.youthpolicy.domain.kleene.Trilean;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 원자 평가 → verdict 사상 — DOMAIN §1.3. out_of_scope는 여기서 다루지 않는다(런타임 평가
 * 이전의 정적 분류이므로 OutOfScopeClassifier·호출부가 Verdict.outOfScope()를 직접 쓴다).
 */
public final class VerdictMapper {

    private VerdictMapper() {
    }

    public static Verdict toVerdict(Trilean combined, List<AtomEvaluation> atomEvaluations) {
        return switch (combined) {
            case TRUE -> Verdict.eligible();
            case FALSE -> Verdict.ineligible();
            case UNKNOWN -> Verdict.needsReview(collectUnknownReasons(atomEvaluations));
        };
    }

    private static List<UnknownReason> collectUnknownReasons(List<AtomEvaluation> atomEvaluations) {
        Set<UnknownReason> reasons = new LinkedHashSet<>();
        for (AtomEvaluation evaluation : atomEvaluations) {
            if (evaluation.outcome().trilean() == Trilean.UNKNOWN) {
                reasons.add(evaluation.outcome().unknownReason());
            }
        }
        return List.copyOf(reasons);
    }
}
