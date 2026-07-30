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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeaseTypeAtomEvaluatorTest {

    private final LeaseTypeAtomEvaluator evaluator = new LeaseTypeAtomEvaluator();
    // DOMAIN.md §4.3: lease_type params {"allowed": ["jeonse"]}
    private final Map<String, Object> params = Map.of("allowed", List.of("jeonse"));

    @Test
    void trueWhenValueInAllowedSet() {
        AtomOutcome outcome = evaluator.evaluate(params, answers(true, "jeonse"));
        assertThat(outcome.trilean()).isEqualTo(Trilean.TRUE);
    }

    @Test
    void falseWhenValueNotInAllowedSet() {
        AtomOutcome outcome = evaluator.evaluate(params, answers(true, "wolse"));
        assertThat(outcome.trilean()).isEqualTo(Trilean.FALSE);
    }

    @Test
    void unknownWhenNotAnswered() {
        AtomOutcome outcome = evaluator.evaluate(params, answers(false, null));
        assertThat(outcome.trilean()).isEqualTo(Trilean.UNKNOWN);
        assertThat(outcome.unknownReason()).isEqualTo(UnknownReason.INPUT_UNCERTAIN);
    }

    @Test
    void failsClosedWhenAllowedParamMissing() {
        // fail-closed: allowed 누락은 규칙 데이터 오류다 — 조용히 통과시키지 않고 즉시 실패한다.
        assertThatThrownBy(() -> evaluator.evaluate(Map.of(), answers(true, "jeonse")))
                .isInstanceOf(IllegalStateException.class);
    }

    private Answers answers(boolean known, String value) {
        return new Answers(
                new AnswerInt(false, null),
                new AnswerBool(false, null),
                new AnswerApproxInt(false, false, null),
                new AnswerString(known, value),
                new AnswerApproxInt(false, false, null));
    }
}
