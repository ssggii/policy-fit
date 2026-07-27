package dev.youthpolicy.domain.evaluate;

import dev.youthpolicy.domain.atom.AtomCatalog;
import dev.youthpolicy.domain.atom.AtomOutcome;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.atom.ValueType;
import dev.youthpolicy.domain.kleene.Trilean;
import dev.youthpolicy.domain.rule.RuleNode;
import dev.youthpolicy.domain.verdict.UnknownReason;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule DSL 트리 전체를 순회하며 Kleene 3치 논리(DOMAIN §3)로 평가한다.
 *
 * 단축평가를 하지 않는다 — all_of/any_of의 모든 자식을 먼저 완전히 평가해 atomEvaluations에
 * 기록한 뒤에야 Trilean.allOf/anyOf로 조합한다. 모든 원자의 충족/미충족을 reasoning(F-004)에
 * 보여줘야 하기 때문이다(과제 요구사항). Trilean.allOf/anyOf 자체의 흡수원소 로직은
 * 이미 계산된 값들의 리스트에 대해서만 적용된다.
 *
 * household_aggregate 원자는 여기 도달하면 안 된다 — OutOfScopeClassifier가 평가 이전에
 * 사전분류해야 한다(ADR-0003 D3). 도달 시 프로그래밍 오류로 간주해 예외를 던진다.
 */
public final class RuleEvaluator {

    public RuleEvaluationResult evaluate(RuleNode root, Answers answers) {
        List<AtomEvaluation> atomEvaluations = new ArrayList<>();
        Trilean value = evaluateNode(root, answers, atomEvaluations);
        return new RuleEvaluationResult(value, atomEvaluations);
    }

    private Trilean evaluateNode(RuleNode node, Answers answers, List<AtomEvaluation> atomEvaluations) {
        return switch (node) {
            case RuleNode.AllOf allOf -> Trilean.allOf(evaluateChildren(allOf.children(), answers, atomEvaluations));
            case RuleNode.AnyOf anyOf -> Trilean.anyOf(evaluateChildren(anyOf.children(), answers, atomEvaluations));
            case RuleNode.Not not -> evaluateNode(not.child(), answers, atomEvaluations).not();
            case RuleNode.AtomRef atomRef -> evaluateAtom(atomRef, answers, atomEvaluations);
        };
    }

    private List<Trilean> evaluateChildren(List<RuleNode> children, Answers answers,
                                            List<AtomEvaluation> atomEvaluations) {
        List<Trilean> results = new ArrayList<>(children.size());
        for (RuleNode child : children) {
            results.add(evaluateNode(child, answers, atomEvaluations));
        }
        return results;
    }

    private Trilean evaluateAtom(RuleNode.AtomRef atomRef, Answers answers, List<AtomEvaluation> atomEvaluations) {
        ValueType valueType = AtomCatalog.valueTypeOf(atomRef.atom());
        AtomOutcome outcome = switch (valueType) {
            case HOUSEHOLD_AGGREGATE -> throw new IllegalStateException(
                    "household_aggregate 원자(" + atomRef.atom()
                            + ")는 OutOfScopeClassifier가 사전분류해야 하며 RuleEvaluator에서 평가될 수 없습니다.");
            // DOMAIN §3.4: admin_discretion 원자는 항상 unknown을 반환한다 — 자가입력 불가이므로
            // evaluator가 필요 없는, 값 유형 자체가 결정하는 도메인 규칙이다.
            case ADMIN_DISCRETION -> AtomOutcome.unknown(UnknownReason.ADMIN_DISCRETION, "행정청의 재량 판단이 필요합니다.");
            case SELF -> AtomCatalog.evaluatorFor(atomRef.atom())
                    .orElseThrow(() -> new UnsupportedOperationException("등록된 평가기가 없는 원자입니다: " + atomRef.atom()))
                    .evaluate(atomRef.params(), answers);
        };
        atomEvaluations.add(new AtomEvaluation(atomRef, outcome));
        return outcome.trilean();
    }
}
