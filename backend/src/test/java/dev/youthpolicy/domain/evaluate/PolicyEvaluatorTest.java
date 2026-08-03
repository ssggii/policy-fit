package dev.youthpolicy.domain.evaluate;

import dev.youthpolicy.domain.atom.AnswerApproxInt;
import dev.youthpolicy.domain.atom.AnswerBool;
import dev.youthpolicy.domain.atom.AnswerInt;
import dev.youthpolicy.domain.atom.AnswerString;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.atom.AtomId;
import dev.youthpolicy.domain.rule.RuleNode;
import dev.youthpolicy.domain.scope.OutOfScopeClassifier;
import dev.youthpolicy.domain.verdict.UnknownReason;
import dev.youthpolicy.domain.verdict.VerdictState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PolicyEvaluator — 정적 out_of_scope + base rule + 범위 게이트(ADR-0005 D5)의 조립·우선순위.
 *
 * <p>테스트 정책: base = all_of[age 19~34 (+ 일부는 income_self)], gate = any_of[age≥30, 혼인].
 */
class PolicyEvaluatorTest {

    private final PolicyEvaluator evaluator =
            new PolicyEvaluator(new OutOfScopeClassifier(), new RuleEvaluator());

    private static final RuleNode BASE_AGE = allOf(atom(AtomId.AGE, Map.of("min", 19, "max", 34)));
    private static final RuleNode BASE_AGE_INCOME = allOf(
            atom(AtomId.AGE, Map.of("min", 19, "max", 34)),
            atom(AtomId.INCOME_SELF, Map.of("max_krw", 50_000_000)));
    private static final RuleNode GATE = anyOf(
            atom(AtomId.AGE, Map.of("min", 30)),
            atom(AtomId.HOUSEHOLD_COMPOSITION, Map.of("scope", "self", "married", true)));

    // ── 게이트 없는 정책: 기존 사상 그대로 ──────────────────────────────

    @Test
    void gateless_baseTrue_eligible() {
        PolicyEvaluation e = evaluator.evaluate(BASE_AGE, null, answers(knownAge(28), unknownIncome(), unknownMarried()));
        assertThat(e.verdict().state()).isEqualTo(VerdictState.ELIGIBLE);
        assertThat(e.reasoning()).hasSize(1); // base age 원자
    }

    @Test
    void gateless_baseFalse_ineligible() {
        PolicyEvaluation e = evaluator.evaluate(BASE_AGE, null, answers(knownAge(40), unknownIncome(), unknownMarried()));
        assertThat(e.verdict().state()).isEqualTo(VerdictState.INELIGIBLE);
    }

    @Test
    void gateless_baseUnknown_needsReview() {
        PolicyEvaluation e = evaluator.evaluate(BASE_AGE_INCOME, null,
                answers(knownAge(28), unknownIncome(), unknownMarried()));
        assertThat(e.verdict().state()).isEqualTo(VerdictState.NEEDS_REVIEW);
        assertThat(e.verdict().unknownReasons()).containsExactly(UnknownReason.INPUT_UNCERTAIN);
    }

    // ── 범위 게이트 있는 정책 ─────────────────────────────────────────

    @Test
    void gated_baseTrue_gateTrueViaAge_eligible() {
        PolicyEvaluation e = evaluator.evaluate(BASE_AGE, GATE, answers(knownAge(32), unknownIncome(), unknownMarried()));
        assertThat(e.verdict().state()).isEqualTo(VerdictState.ELIGIBLE);
    }

    @Test
    void gated_baseTrue_gateTrueViaMarried_eligible() {
        PolicyEvaluation e = evaluator.evaluate(BASE_AGE, GATE, answers(knownAge(28), unknownIncome(), knownMarried(true)));
        assertThat(e.verdict().state()).isEqualTo(VerdictState.ELIGIBLE);
    }

    @Test
    void gated_baseTrue_gateFalse_outOfScope() {
        // 28세·미혼(둘 다 확정) → 자가 면제 불가 → out_of_scope (ADR-0005 D5 핵심)
        PolicyEvaluation e = evaluator.evaluate(BASE_AGE, GATE, answers(knownAge(28), unknownIncome(), knownMarried(false)));
        assertThat(e.verdict().state()).isEqualTo(VerdictState.OUT_OF_SCOPE);
        assertThat(e.reasoning()).isEmpty(); // 범위 밖은 자격 요건을 노출하지 않는다
    }

    @Test
    void gated_baseTrue_gateUnknownViaMarried_needsReview() {
        // 혼인 여부 '모름' → 게이트 UNKNOWN → needs_review (out_of_scope 아님)
        PolicyEvaluation e = evaluator.evaluate(BASE_AGE, GATE, answers(knownAge(28), unknownIncome(), unknownMarried()));
        assertThat(e.verdict().state()).isEqualTo(VerdictState.NEEDS_REVIEW);
        assertThat(e.verdict().unknownReasons()).containsExactly(UnknownReason.INPUT_UNCERTAIN);
        assertThat(e.reasoning()).hasSize(1); // reasoning은 base(age)만 — 게이트 원자는 제외
    }

    @Test
    void gated_baseFalse_ineligibleBeatsGate() {
        // 40세: base 탈락이 게이트(age≥30 TRUE)보다 우선 → out_of_scope가 아니라 ineligible
        PolicyEvaluation e = evaluator.evaluate(BASE_AGE, GATE, answers(knownAge(40), unknownIncome(), knownMarried(true)));
        assertThat(e.verdict().state()).isEqualTo(VerdictState.INELIGIBLE);
    }

    @Test
    void gated_baseUnknown_gateTrue_needsReview() {
        // 소득 '모름'(base UNKNOWN) + 게이트 TRUE(32세) → needs_review
        PolicyEvaluation e = evaluator.evaluate(BASE_AGE_INCOME, GATE,
                answers(knownAge(32), unknownIncome(), unknownMarried()));
        assertThat(e.verdict().state()).isEqualTo(VerdictState.NEEDS_REVIEW);
        assertThat(e.verdict().unknownReasons()).containsExactly(UnknownReason.INPUT_UNCERTAIN);
    }

    @Test
    void gated_baseUnknown_gateUnknown_needsReview() {
        // 소득 '모름'(base UNKNOWN) + 혼인 '모름'(gate UNKNOWN) → needs_review. 두 사유가 모두
        // input_uncertain이라 dedup되어 1개.
        PolicyEvaluation e = evaluator.evaluate(BASE_AGE_INCOME, GATE,
                answers(knownAge(26), unknownIncome(), unknownMarried()));
        assertThat(e.verdict().state()).isEqualTo(VerdictState.NEEDS_REVIEW);
        assertThat(e.verdict().unknownReasons()).containsExactly(UnknownReason.INPUT_UNCERTAIN);
    }

    @Test
    void gated_householdAggregateInGate_failsLoud() {
        // 게이트에 household_aggregate 원자가 들어가면 RuleEvaluator가 fail-loud로 막는다
        // (정적 분류는 base rule에만 적용되므로 게이트는 런타임 방어에 의존 — ADR-0003 D3/D4).
        RuleNode aggregateGate = anyOf(atom(AtomId.INCOME_HOUSEHOLD, Map.of()));
        assertThatThrownBy(() -> evaluator.evaluate(BASE_AGE, aggregateGate,
                answers(knownAge(28), unknownIncome(), unknownMarried())))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── 정적 out_of_scope(D3)는 게이트 유무와 무관하게 우선 ──────────────

    @Test
    void staticOutOfScope_householdAggregateInBaseRule() {
        RuleNode ruleWithAggregate = allOf(atom(AtomId.INCOME_HOUSEHOLD, Map.of()));
        PolicyEvaluation e = evaluator.evaluate(ruleWithAggregate, GATE,
                answers(knownAge(28), unknownIncome(), knownMarried(false)));
        assertThat(e.verdict().state()).isEqualTo(VerdictState.OUT_OF_SCOPE);
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────

    private static RuleNode atom(AtomId id, Map<String, Object> params) {
        return new RuleNode.AtomRef(id, params, null);
    }

    private static RuleNode allOf(RuleNode... children) {
        return new RuleNode.AllOf(List.of(children));
    }

    private static RuleNode anyOf(RuleNode... children) {
        return new RuleNode.AnyOf(List.of(children));
    }

    private static Answers answers(AnswerInt age, AnswerApproxInt income, AnswerBool married) {
        return new Answers(age, new AnswerBool(false, null), income, new AnswerString(false, null),
                new AnswerApproxInt(false, false, null), married);
    }

    private static AnswerInt knownAge(int value) {
        return new AnswerInt(true, value);
    }

    private static AnswerApproxInt unknownIncome() {
        return new AnswerApproxInt(false, false, null);
    }

    private static AnswerBool knownMarried(boolean value) {
        return new AnswerBool(true, value);
    }

    private static AnswerBool unknownMarried() {
        return new AnswerBool(false, null);
    }
}
