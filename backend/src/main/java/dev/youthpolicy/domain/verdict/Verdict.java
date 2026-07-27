package dev.youthpolicy.domain.verdict;

import java.util.List;

/**
 * 판정 결과 — contracts/verdict.schema.json의 도메인 표현.
 *
 * compact constructor로 스키마의 if/then/else를 컴파일타임이 아닌 생성 시점에 강제한다:
 * state=needs_review일 때만, 그리고 그때만 unknownReasons가 최소 1개 있어야 한다.
 */
public record Verdict(VerdictState state, List<UnknownReason> unknownReasons) {

    public Verdict {
        if (state == null) {
            throw new IllegalArgumentException("state는 필수입니다.");
        }
        if (state == VerdictState.NEEDS_REVIEW) {
            if (unknownReasons == null || unknownReasons.isEmpty()) {
                throw new IllegalArgumentException(
                        "needs_review 상태는 unknown_reasons가 최소 1개 필요합니다 (verdict.schema.json if/then).");
            }
            unknownReasons = List.copyOf(unknownReasons);
        } else {
            if (unknownReasons != null && !unknownReasons.isEmpty()) {
                throw new IllegalArgumentException(
                        state.wireValue() + " 상태는 unknown_reasons를 가질 수 없습니다 (verdict.schema.json else).");
            }
            unknownReasons = List.of();
        }
    }

    public static Verdict eligible() {
        return new Verdict(VerdictState.ELIGIBLE, List.of());
    }

    public static Verdict ineligible() {
        return new Verdict(VerdictState.INELIGIBLE, List.of());
    }

    public static Verdict outOfScope() {
        return new Verdict(VerdictState.OUT_OF_SCOPE, List.of());
    }

    public static Verdict needsReview(List<UnknownReason> unknownReasons) {
        return new Verdict(VerdictState.NEEDS_REVIEW, unknownReasons);
    }
}
