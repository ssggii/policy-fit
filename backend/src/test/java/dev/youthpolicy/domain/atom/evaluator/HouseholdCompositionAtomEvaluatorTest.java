package dev.youthpolicy.domain.atom.evaluator;

import dev.youthpolicy.domain.atom.AnswerApproxInt;
import dev.youthpolicy.domain.atom.AnswerBool;
import dev.youthpolicy.domain.atom.AnswerInt;
import dev.youthpolicy.domain.atom.AnswerString;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.atom.AtomOutcome;
import dev.youthpolicy.domain.kleene.Trilean;
import dev.youthpolicy.domain.verdict.UnknownReason;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** household_composition 원자 — scope:self + married 요건(ADR-0005 D5) 및 fail-loud(ADR-0003 D4). */
class HouseholdCompositionAtomEvaluatorTest {

    private final HouseholdCompositionAtomEvaluator evaluator = new HouseholdCompositionAtomEvaluator();

    private static final Map<String, Object> SELF_MARRIED = Map.of("scope", "self", "married", true);

    @Test
    void marriedTrue_meetsMarriedRequirement() {
        AtomOutcome outcome = evaluator.evaluate(SELF_MARRIED, answersMarried(true, true));
        assertThat(outcome.trilean()).isEqualTo(Trilean.TRUE);
    }

    @Test
    void marriedFalse_failsMarriedRequirement() {
        AtomOutcome outcome = evaluator.evaluate(SELF_MARRIED, answersMarried(true, false));
        assertThat(outcome.trilean()).isEqualTo(Trilean.FALSE);
    }

    @Test
    void marriedUnknown_yieldsInputUncertainUnknown() {
        AtomOutcome outcome = evaluator.evaluate(SELF_MARRIED, answersMarried(false, null));
        assertThat(outcome.trilean()).isEqualTo(Trilean.UNKNOWN);
        assertThat(outcome.unknownReason()).isEqualTo(UnknownReason.INPUT_UNCERTAIN);
    }

    @Test
    void missingMarriedParam_failsLoud() {
        assertThatThrownBy(() -> evaluator.evaluate(Map.of("scope", "self"), answersMarried(true, true)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonSelfScope_failsLoud() {
        assertThatThrownBy(() -> evaluator.evaluate(Map.of("scope", "aggregate", "married", true),
                answersMarried(true, true)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void missingScope_failsLoud() {
        assertThatThrownBy(() -> evaluator.evaluate(Map.of("married", true), answersMarried(true, true)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static Answers answersMarried(boolean known, Boolean value) {
        return new Answers(
                new AnswerInt(false, null),
                new AnswerBool(false, null),
                new AnswerApproxInt(false, false, null),
                new AnswerString(false, null),
                new AnswerApproxInt(false, false, null),
                new AnswerBool(known, value));
    }
}
