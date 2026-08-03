package dev.youthpolicy.domain.evaluate;

import dev.youthpolicy.domain.atom.AnswerApproxInt;
import dev.youthpolicy.domain.atom.AnswerBool;
import dev.youthpolicy.domain.atom.AnswerInt;
import dev.youthpolicy.domain.atom.AnswerString;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.atom.AtomId;
import dev.youthpolicy.domain.kleene.Trilean;
import dev.youthpolicy.domain.rule.RuleNode;
import dev.youthpolicy.domain.verdict.UnknownReason;
import dev.youthpolicy.domain.verdict.Verdict;
import dev.youthpolicy.domain.verdict.VerdictMapper;
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
    void allOf_withNestedAnyOfAbsorbingUnknown_excludesAbsorbedAtomFromContributing() {
        // any_of(income_self=true, separate_residence=unknown) → true(흡수) 그대로 all_of의 age=unknown과 조합되면
        // 전체는 unknown이지만, 흡수된 separate_residence의 unknown은 결과에 기여하지 않았으므로 제외돼야 한다(#8).
        RuleNode rule = new RuleNode.AllOf(List.of(
                new RuleNode.AnyOf(List.of(INCOME_SELF_50M, SEPARATE_RESIDENCE)),
                AGE_19_34));
        Answers answers = answers(unknownAge(), knownIncome(1_000_000)); // 연 1,200만원 <= 5천만원(true), 나이 모름

        RuleEvaluationResult result = evaluator.evaluate(rule, answers);

        assertThat(result.value()).isEqualTo(Trilean.UNKNOWN);
        assertThat(result.atomEvaluations())
                .as("reasoning(F-004)에는 흡수 여부와 무관하게 평가된 원자가 전부 남아야 한다")
                .hasSize(3);
        assertThat(result.contributingEvaluations())
                .extracting(evaluation -> evaluation.atomRef().atom())
                .as("age만 needs_review를 실제로 결정했고, income_self=true에 흡수된 separate_residence는 무관하다")
                .containsExactly(AtomId.AGE);

        Verdict verdict = VerdictMapper.toVerdict(result.value(), result.contributingEvaluations());
        assertThat(verdict.unknownReasons()).containsExactly(UnknownReason.INPUT_UNCERTAIN);
    }

    @Test
    void anyOf_withNestedAllOfAbsorbingUnknown_excludesAbsorbedAtomFromContributing() {
        // all_of(income_self=false, separate_residence=unknown) → false(흡수) 그대로 any_of의 age=unknown과 조합되면
        // 전체는 unknown이지만, 흡수된 separate_residence의 unknown은 결과에 기여하지 않았으므로 제외돼야 한다.
        RuleNode rule = new RuleNode.AnyOf(List.of(
                new RuleNode.AllOf(List.of(INCOME_SELF_50M, SEPARATE_RESIDENCE)),
                AGE_19_34));
        Answers answers = answers(unknownAge(), knownIncome(10_000_000)); // 연 1.2억원 > 5천만원(false), 나이 모름

        RuleEvaluationResult result = evaluator.evaluate(rule, answers);

        assertThat(result.value()).isEqualTo(Trilean.UNKNOWN);
        assertThat(result.contributingEvaluations())
                .extracting(evaluation -> evaluation.atomRef().atom())
                .as("age만 needs_review를 실제로 결정했고, income_self=false에 흡수된 separate_residence는 무관하다")
                .containsExactly(AtomId.AGE);
    }

    @Test
    void not_overNestedAbsorption_passesContributingThroughUnchanged() {
        // not(any_of(income_self=false, separate_residence=unknown))의 any_of는 income_self=false가 흡수되고
        // separate_residence의 unknown만 남는다. not은 값만 뒤집을 뿐 그 책임 원자 집합은 그대로 전달해야 한다.
        RuleNode rule = new RuleNode.AllOf(List.of(
                new RuleNode.Not(new RuleNode.AnyOf(List.of(INCOME_SELF_50M, SEPARATE_RESIDENCE))),
                AGE_19_34));
        Answers answers = answers(unknownAge(), knownIncome(10_000_000)); // 연 1.2억원 > 5천만원(false), 나이 모름

        RuleEvaluationResult result = evaluator.evaluate(rule, answers);

        assertThat(result.value()).isEqualTo(Trilean.UNKNOWN);
        assertThat(result.contributingEvaluations())
                .extracting(evaluation -> evaluation.atomRef().atom())
                .as("not 안쪽에서 income_self=false에 흡수된 원자는 없고, separate_residence(any_of의 unknown 유일 책임)와"
                        + " age가 함께 최종 unknown을 결정한다")
                .containsExactlyInAnyOrder(AtomId.SEPARATE_RESIDENCE, AtomId.AGE);
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
                new AnswerApproxInt(false, false, null),
                new AnswerBool(false, null));
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
