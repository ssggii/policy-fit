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

class AssetSelfAtomEvaluatorTest {

    private final AssetSelfAtomEvaluator evaluator = new AssetSelfAtomEvaluator();
    private final Map<String, Object> params = Map.of("max_krw", 34_500_000);

    @Test
    void trueAtExactBoundary() {
        AtomOutcome outcome = evaluator.evaluate(params, answers(true, false, 34_500_000));
        assertThat(outcome.trilean()).isEqualTo(Trilean.TRUE);
    }

    @Test
    void falseJustOverBoundary() {
        AtomOutcome outcome = evaluator.evaluate(params, answers(true, false, 34_500_001));
        assertThat(outcome.trilean()).isEqualTo(Trilean.FALSE);
    }

    @Test
    void unknownWhenApproxRegardlessOfValue() {
        // DOMAIN §3.4: self(느슨) 원자라도 '모름/대략'은 입력 불확실로 분류 — 자동 해소 금지.
        AtomOutcome outcome = evaluator.evaluate(params, answers(true, true, 1_000_000));
        assertThat(outcome.trilean()).isEqualTo(Trilean.UNKNOWN);
        assertThat(outcome.unknownReason()).isEqualTo(UnknownReason.INPUT_UNCERTAIN);
    }

    @Test
    void unknownWhenNotAnswered() {
        AtomOutcome outcome = evaluator.evaluate(params, answers(false, false, null));
        assertThat(outcome.trilean()).isEqualTo(Trilean.UNKNOWN);
        assertThat(outcome.unknownReason()).isEqualTo(UnknownReason.INPUT_UNCERTAIN);
    }

    @Test
    void failsClosedWhenMaxKrwParamMissing() {
        // fail-closed: max_krw 누락은 규칙 데이터 오류다 — 조용히 TRUE로 통과시키지 않고 즉시 실패한다.
        assertThatThrownBy(() -> evaluator.evaluate(Map.of(), answers(true, false, 1_000_000)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Answers answers(boolean known, boolean approx, Integer value) {
        return new Answers(
                new AnswerInt(false, null),
                new AnswerBool(false, null),
                new AnswerApproxInt(false, false, null),
                new AnswerString(false, null),
                new AnswerApproxInt(known, approx, value));
    }
}
