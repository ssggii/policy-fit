package dev.youthpolicy.domain.evaluate;

import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.kleene.Trilean;
import dev.youthpolicy.domain.rule.RuleNode;
import dev.youthpolicy.domain.scope.OutOfScopeClassifier;
import dev.youthpolicy.domain.verdict.Verdict;
import dev.youthpolicy.domain.verdict.VerdictMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 정책 하나를 최종 판정으로 조립하는 도메인 진입점 — 정적 out_of_scope 사전분류(ADR-0003 D3) +
 * base rule Kleene 평가(§3) + 범위 게이트(ADR-0005 D5)를 한 곳에서 엮는다. 컨트롤러·골든 러너가
 * 공통으로 호출한다.
 *
 * <p>판정 순서(ADR-0005 D5):
 * <pre>
 *   1. household_aggregate 정적 사전분류        → out_of_scope
 *   2. base rule 평가
 *        base == FALSE                          → ineligible (자가판정 확정 탈락, 게이트보다 우선)
 *        base ∈ {TRUE, UNKNOWN}:
 *           게이트 없음                          → base verdict
 *           게이트 있음:
 *              gate == FALSE                     → out_of_scope (자가 면제 불가)
 *              gate == TRUE  & base == TRUE      → eligible
 *              그 외(base 또는 gate가 UNKNOWN)   → needs_review (unknown 기여분 수집)
 * </pre>
 *
 * <p>reasoning(F-004)에는 자격 요건(base rule) 원자만 싣는다 — 게이트는 자격이 아니라 범위를 가르는
 * 로직이므로(§4.4) reasoning 목록의 의미를 자격 요건 결과로 유지한다. 게이트발 needs_review의 원인은
 * verdict.unknown_reasons 태그(§3.1)로 전달된다.
 */
public final class PolicyEvaluator {

    private final OutOfScopeClassifier outOfScopeClassifier;
    private final RuleEvaluator ruleEvaluator;

    public PolicyEvaluator(OutOfScopeClassifier outOfScopeClassifier, RuleEvaluator ruleEvaluator) {
        this.outOfScopeClassifier = outOfScopeClassifier;
        this.ruleEvaluator = ruleEvaluator;
    }

    public PolicyEvaluation evaluate(RuleNode rule, RuleNode outOfScopeGate, Answers answers) {
        // 1. 정적 out_of_scope — 답변을 평가하지 않는다(ADR-0003 D3).
        if (outOfScopeClassifier.isOutOfScope(rule)) {
            return new PolicyEvaluation(Verdict.outOfScope(), List.of());
        }

        RuleEvaluationResult base = ruleEvaluator.evaluate(rule, answers);

        // 2. 게이트 없는 정책 — 기존 사상(§1.3) 그대로.
        if (outOfScopeGate == null) {
            Verdict verdict = VerdictMapper.toVerdict(base.value(), base.contributingEvaluations());
            return new PolicyEvaluation(verdict, base.atomEvaluations());
        }

        // 3-a. base FALSE는 자가판정 확정 탈락 → 게이트보다 우선(ineligible, ADR-0005 D5).
        if (base.value() == Trilean.FALSE) {
            return new PolicyEvaluation(Verdict.ineligible(), base.atomEvaluations());
        }

        RuleEvaluationResult gate = ruleEvaluator.evaluate(outOfScopeGate, answers);

        // 3-b. 게이트 FALSE → 자가 면제 불가 → out_of_scope.
        if (gate.value() == Trilean.FALSE) {
            return new PolicyEvaluation(Verdict.outOfScope(), List.of());
        }

        // 3-c. 게이트 TRUE/UNKNOWN → base·gate의 UNKNOWN 기여분으로 eligible/needs_review 결정.
        List<AtomEvaluation> unknownContributors = new ArrayList<>();
        if (base.value() == Trilean.UNKNOWN) {
            unknownContributors.addAll(base.contributingEvaluations());
        }
        if (gate.value() == Trilean.UNKNOWN) {
            unknownContributors.addAll(gate.contributingEvaluations());
        }

        Verdict verdict = unknownContributors.isEmpty()
                ? Verdict.eligible() // base == TRUE && gate == TRUE
                : VerdictMapper.toVerdict(Trilean.UNKNOWN, unknownContributors);
        return new PolicyEvaluation(verdict, base.atomEvaluations());
    }
}
