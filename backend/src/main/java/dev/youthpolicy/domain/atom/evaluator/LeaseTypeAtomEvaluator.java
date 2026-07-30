package dev.youthpolicy.domain.atom.evaluator;

import dev.youthpolicy.domain.atom.AnswerString;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.atom.AtomEvaluator;
import dev.youthpolicy.domain.atom.AtomOutcome;
import dev.youthpolicy.domain.kleene.Trilean;
import dev.youthpolicy.domain.verdict.UnknownReason;

import java.util.List;
import java.util.Map;

/**
 * {@code lease_type} 원자 — DOMAIN §2.3·§4.3: params {@code {allowed: [...]}}에 답변 임차 형태가
 * 포함되는지 여부. 예: 전세 한정 정책은 {@code {"allowed": ["jeonse"]}}.
 */
public final class LeaseTypeAtomEvaluator implements AtomEvaluator {

    @Override
    public AtomOutcome evaluate(Map<String, Object> params, Answers answers) {
        AnswerString leaseType = answers == null ? null : answers.leaseType();
        if (leaseType == null || !leaseType.known() || leaseType.value() == null) {
            return AtomOutcome.unknown(UnknownReason.INPUT_UNCERTAIN, "임차 형태를 확인할 수 없습니다.");
        }

        // fail-closed: allowed 누락은 규칙 데이터 오류다. 조용히 TRUE로 통과시키면 임차 형태
        // 요건이 무력화된 채 판정되므로, 정책 JSON이 잘못됐다는 신호로 즉시 실패시킨다.
        List<String> allowed = allowedValues(params);
        if (allowed.isEmpty()) {
            throw new IllegalStateException("lease_type 원자에 allowed 파라미터가 없습니다.");
        }

        if (allowed.contains(leaseType.value())) {
            return AtomOutcome.of(Trilean.TRUE, "임차 형태가 허용 범위에 포함됩니다.");
        }
        return AtomOutcome.of(Trilean.FALSE, "임차 형태가 허용 범위에 포함되지 않습니다.");
    }

    private List<String> allowedValues(Map<String, Object> params) {
        if (params == null) {
            return List.of();
        }
        Object raw = params.get("allowed");
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        return list.stream().map(String::valueOf).toList();
    }
}
