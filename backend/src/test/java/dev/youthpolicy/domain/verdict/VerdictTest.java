package dev.youthpolicy.domain.verdict;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** needs_review인데 reasons가 비어있으면 생성 실패해야 한다(verdict.schema.json if/then/else). */
class VerdictTest {

    @Test
    void needsReviewRequiresAtLeastOneReason() {
        assertThatThrownBy(() -> new Verdict(VerdictState.NEEDS_REVIEW, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void needsReviewRequiresNonNullReasons() {
        assertThatThrownBy(() -> new Verdict(VerdictState.NEEDS_REVIEW, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void eligibleMustNotCarryReasons() {
        assertThatThrownBy(() -> new Verdict(VerdictState.ELIGIBLE, List.of(UnknownReason.BOUNDARY)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ineligibleMustNotCarryReasons() {
        assertThatThrownBy(() -> new Verdict(VerdictState.INELIGIBLE, List.of(UnknownReason.ADMIN_DISCRETION)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void outOfScopeMustNotCarryReasons() {
        assertThatThrownBy(() -> new Verdict(VerdictState.OUT_OF_SCOPE, List.of(UnknownReason.INPUT_UNCERTAIN)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void eligibleWithoutReasonsIsValid() {
        Verdict verdict = Verdict.eligible();
        assertThat(verdict.state()).isEqualTo(VerdictState.ELIGIBLE);
        assertThat(verdict.unknownReasons()).isEmpty();
    }

    @Test
    void needsReviewWithReasonsIsValid() {
        Verdict verdict = Verdict.needsReview(List.of(UnknownReason.BOUNDARY, UnknownReason.INPUT_UNCERTAIN));
        assertThat(verdict.state()).isEqualTo(VerdictState.NEEDS_REVIEW);
        assertThat(verdict.unknownReasons()).containsExactly(UnknownReason.BOUNDARY, UnknownReason.INPUT_UNCERTAIN);
    }
}
