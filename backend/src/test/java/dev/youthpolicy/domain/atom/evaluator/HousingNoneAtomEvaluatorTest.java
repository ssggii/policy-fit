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

class HousingNoneAtomEvaluatorTest {

    private final HousingNoneAtomEvaluator evaluator = new HousingNoneAtomEvaluator();

    @Test
    void trueWhenNoHousing() {
        AtomOutcome outcome = evaluator.evaluate(Map.of(), answers(true, true));
        assertThat(outcome.trilean()).isEqualTo(Trilean.TRUE);
    }

    @Test
    void falseWhenOwnsHousing() {
        AtomOutcome outcome = evaluator.evaluate(Map.of(), answers(true, false));
        assertThat(outcome.trilean()).isEqualTo(Trilean.FALSE);
    }

    @Test
    void unknownWhenNotAnswered() {
        AtomOutcome outcome = evaluator.evaluate(Map.of(), answers(false, null));
        assertThat(outcome.trilean()).isEqualTo(Trilean.UNKNOWN);
        assertThat(outcome.unknownReason()).isEqualTo(UnknownReason.INPUT_UNCERTAIN);
    }

    private Answers answers(boolean known, Boolean value) {
        return new Answers(
                new AnswerInt(false, null),
                new AnswerBool(known, value),
                new AnswerApproxInt(false, false, null),
                new AnswerString(false, null),
                new AnswerApproxInt(false, false, null),
                new AnswerBool(false, null));
    }
}
