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

    /**
     * @param contributingEvaluations combined 값을 실제로 결정한(흡수되지 않은) 원자 평가만
     *                                 — {@link dev.youthpolicy.domain.evaluate.RuleEvaluationResult#contributingEvaluations()}.
     *                                 흡수된 원자(예: any_of에서 true인 형제에 흡수된 unknown)는 여기 없어야 한다.
     */
    public static Verdict toVerdict(Trilean combined, List<AtomEvaluation> contributingEvaluations) {
        return switch (combined) {
            case TRUE -> Verdict.eligible();
            case FALSE -> Verdict.ineligible();
            case UNKNOWN -> Verdict.needsReview(collectUnknownReasons(contributingEvaluations));
        };
    }

    private static List<UnknownReason> collectUnknownReasons(List<AtomEvaluation> contributingEvaluations) {
        Set<UnknownReason> reasons = new LinkedHashSet<>();
        for (AtomEvaluation evaluation : contributingEvaluations) {
            if (evaluation.outcome().trilean() == Trilean.UNKNOWN) {
                reasons.add(evaluation.outcome().unknownReason());
            }
        }
        return List.copyOf(reasons);
    }
}
