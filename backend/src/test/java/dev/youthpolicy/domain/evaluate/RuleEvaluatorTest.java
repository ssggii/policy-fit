package dev.youthpolicy.domain.evaluate;

import dev.youthpolicy.domain.atom.AnswerApproxInt;
import dev.youthpolicy.domain.atom.AnswerBool;
import dev.youthpolicy.domain.atom.AnswerInt;
import dev.youthpolicy.domain.atom.AnswerString;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.atom.AtomId;
import dev.youthpolicy.domain.kleene.Trilean;
import dev.youthpolicy.domain.rule.RuleNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** DOMAIN.md §3.5 경계 예시 4개를 RuleEvaluator로 그대로 검증한다. */
class RuleEvaluatorTest {

    private final RuleEvaluator evaluator = new RuleEvaluator();

    // age: self, income_self: self, separate_residence: admin_discretion(§3.5 예시의 "admin_discretion" 원자에 대응)
    private static final RuleNode.AtomRef AGE_19_34 =
            new RuleNode.AtomRef(AtomId.AGE, Map.of("min", 19, "max", 34), null);
    private static final RuleNode.AtomRef INCOME_SELF_50M =
            new RuleNode.AtomRef(AtomId.INCOME_SELF, Map.of("max_krw", 50_000_000), null);
    private static final RuleNode.AtomRef SEPARATE_RESIDENCE =
            new RuleNode.AtomRef(AtomId.SEPARATE_RESIDENCE, Map.of(), null);

    @Test
    void allOf_ageTrue_incomeUnknown_isUnknown() {
        // DOMAIN §3.5: all_of(age=true, income_self=unknown) → unknown → needs_review
        RuleNode rule = new RuleNode.AllOf(List.of(AGE_19_34, INCOME_SELF_50M));
        Answers answers = answers(knownAge(25), unknownIncome());

        assertThat(evaluator.evaluate(rule, answers).value()).isEqualTo(Trilean.UNKNOWN);
    }

    @Test
    void allOf_ageFalse_incomeUnknown_isFalse() {
        // DOMAIN §3.5: all_of(age=false, income_self=unknown) → false → 부적합(나이에서 이미 탈락)
        RuleNode rule = new RuleNode.AllOf(List.of(AGE_19_34, INCOME_SELF_50M));
        Answers answers = answers(knownAge(10), unknownIncome());

        assertThat(evaluator.evaluate(rule, answers).value()).isEqualTo(Trilean.FALSE);
    }

    @Test
    void anyOf_incomeTrue_adminDiscretionUnknown_isTrue() {
        // DOMAIN §3.5: any_of(income_self=true, admin_discretion=unknown) → true → 가능(대체 경로 해소)
        RuleNode rule = new RuleNode.AnyOf(List.of(INCOME_SELF_50M, SEPARATE_RESIDENCE));
        Answers answers = answers(unknownAge(), knownIncome(1_000_000)); // 연 1,200만원 <= 5천만원

        assertThat(evaluator.evaluate(rule, answers).value()).isEqualTo(Trilean.TRUE);
    }

    @Test
    void anyOf_incomeFalse_adminDiscretionUnknown_isUnknown() {
        // DOMAIN §3.5: any_of(income_self=false, admin_discretion=unknown) → unknown → needs_review(행정 재량 확인)
        RuleNode rule = new RuleNode.AnyOf(List.of(INCOME_SELF_50M, SEPARATE_RESIDENCE));
        Answers answers = answers(unknownAge(), knownIncome(10_000_000)); // 연 1.2억원 > 5천만원

        assertThat(evaluator.evaluate(rule, answers).value()).isEqualTo(Trilean.UNKNOWN);
    }

    @Test
    void householdAggregateAtomReachingEvaluatorIsAProgrammingError() {
        // OutOfScopeClassifier가 사전분류해야 할 원자가 실수로 RuleEvaluator까지 온 경우 방어적으로 실패한다.
        RuleNode rule = new RuleNode.AtomRef(AtomId.INCOME_HOUSEHOLD, Map.of(), null);
        Answers answers = answers(unknownAge(), unknownIncome());

        assertThatThrownBy(() -> evaluator.evaluate(rule, answers))
                .isInstanceOf(IllegalStateException.class);
    }

    private Answers answers(AnswerInt age, AnswerApproxInt income) {
        return new Answers(
                age,
                new AnswerBool(false, null),
                income,
                new AnswerString(false, null),
                new AnswerApproxInt(false, false, null));
    }

    private AnswerInt knownAge(int value) {
        return new AnswerInt(true, value);
    }

    private AnswerInt unknownAge() {
        return new AnswerInt(false, null);
    }

    private AnswerApproxInt knownIncome(int monthlyValue) {
        return new AnswerApproxInt(true, false, monthlyValue);
    }

    private AnswerApproxInt unknownIncome() {
        return new AnswerApproxInt(false, false, null);
    }
}
