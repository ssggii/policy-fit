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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgeAtomEvaluatorTest {

    private final AgeAtomEvaluator evaluator = new AgeAtomEvaluator();
    private final Map<String, Object> params = Map.of("min", 19, "max", 34);

    @ParameterizedTest(name = "age={0} -> {1}")
    @CsvSource({
            "18, FALSE", // 하한 미만
            "19, TRUE",  // 하한 포함
            "34, TRUE",  // 상한 포함
            "35, FALSE"  // 상한 초과
    })
    void boundaryValues(int age, Trilean expected) {
        AtomOutcome outcome = evaluator.evaluate(params, answersWithAge(true, age));
        assertThat(outcome.trilean()).isEqualTo(expected);
    }

    @Test
    void unknownWhenNotAnswered() {
        AtomOutcome outcome = evaluator.evaluate(params, answersWithAge(false, null));
        assertThat(outcome.trilean()).isEqualTo(Trilean.UNKNOWN);
        assertThat(outcome.unknownReason()).isEqualTo(UnknownReason.INPUT_UNCERTAIN);
    }

    private Answers answersWithAge(boolean known, Integer value) {
        return new Answers(
                new AnswerInt(known, value),
                new AnswerBool(false, null),
                new AnswerApproxInt(false, false, null),
                new AnswerString(false, null),
                new AnswerApproxInt(false, false, null));
    }
}
