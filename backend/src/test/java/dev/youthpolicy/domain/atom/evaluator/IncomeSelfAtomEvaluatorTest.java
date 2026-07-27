package dev.youthpolicy.domain.atom.evaluator;

import dev.youthpolicy.domain.atom.AnswerApproxInt;
import dev.youthpolicy.domain.atom.AnswerBool;
import dev.youthpolicy.domain.atom.AnswerInt;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.atom.AtomOutcome;
import dev.youthpolicy.domain.kleene.Trilean;
import dev.youthpolicy.domain.verdict.UnknownReason;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IncomeSelfAtomEvaluatorTest {

    private final IncomeSelfAtomEvaluator evaluator = new IncomeSelfAtomEvaluator();
    // 연 3,600만원 상한 (월 300만원 x 12 로 딱 떨어지는 값으로 경계 테스트)
    private final Map<String, Object> params = Map.of("max_krw", 36_000_000);

    @Test
    void trueAtExactBoundary() {
        // 월 300만원 x 12 = 연 3,600만원 = 상한과 동일 → "이하" 조건 충족
        AtomOutcome outcome = evaluator.evaluate(params, answers(true, false, 3_000_000));
        assertThat(outcome.trilean()).isEqualTo(Trilean.TRUE);
    }

    @Test
    void falseJustOverBoundary() {
        AtomOutcome outcome = evaluator.evaluate(params, answers(true, false, 3_000_001));
        assertThat(outcome.trilean()).isEqualTo(Trilean.FALSE);
    }

    @Test
    void unknownWhenApproxRegardlessOfValue() {
        // approx=true는 값과 무관하게 항상 UNKNOWN(input_uncertain) — 자동으로 true/false에 해소하지 않는다.
        // DOMAIN §3.4: self 원자의 '모름/대략' 입력은 함께 입력 불확실로 분류된다.
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

    private Answers answers(boolean known, boolean approx, Integer value) {
        return new Answers(
                new AnswerInt(false, null),
                new AnswerBool(false, null),
                new AnswerApproxInt(known, approx, value));
    }
}
