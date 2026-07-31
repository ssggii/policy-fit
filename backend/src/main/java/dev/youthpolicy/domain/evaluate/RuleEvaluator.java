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
import java.util.function.Function;

/**
 * Rule DSL 트리 전체를 순회하며 Kleene 3치 논리(DOMAIN §3)로 평가한다.
 *
 * 단축평가를 하지 않는다 — all_of/any_of의 모든 자식을 먼저 완전히 평가해 atomEvaluations에
 * 기록한 뒤에야 Trilean.allOf/anyOf로 조합한다. 모든 원자의 충족/미충족을 reasoning(F-004)에
 * 보여줘야 하기 때문이다(과제 요구사항). Trilean.allOf/anyOf 자체의 흡수원소 로직은
 * 이미 계산된 값들의 리스트에 대해서만 적용된다.
 *
 * 흡수원소로 인해 결과에 기여하지 않은 원자는 needs_review 사유(unknown_reasons)에서
 * 제외돼야 한다(예: any_of(true, unknown)의 unknown은 무관 — DOMAIN §3.5). 이를 위해 각
 * 노드가 자기 값뿐 아니라 그 값을 실제로 결정한(흡수되지 않은) 원자 평가들도 함께 반환하며,
 * 상위 노드로 재귀 전파된다(root의 결과 = {@link RuleEvaluationResult#contributingEvaluations()}).
 *
 * household_aggregate 원자는 여기 도달하면 안 된다 — OutOfScopeClassifier가 평가 이전에
 * 사전분류해야 한다(ADR-0003 D3). 도달 시 프로그래밍 오류로 간주해 예외를 던진다.
 */
public final class RuleEvaluator {

    /** 노드 하나의 평가 결과 — 값과, 그 값을 실제로 결정한(흡수되지 않은) 원자 평가들. */
    private record NodeResult(Trilean value, List<AtomEvaluation> contributing) {
    }

    public RuleEvaluationResult evaluate(RuleNode root, Answers answers) {
        List<AtomEvaluation> atomEvaluations = new ArrayList<>();
        NodeResult result = evaluateNode(root, answers, atomEvaluations);
        return new RuleEvaluationResult(result.value(), atomEvaluations, result.contributing());
    }

    private NodeResult evaluateNode(RuleNode node, Answers answers, List<AtomEvaluation> atomEvaluations) {
        return switch (node) {
            case RuleNode.AllOf allOf ->
                    combine(evaluateChildren(allOf.children(), answers, atomEvaluations), Trilean::allOf);
            case RuleNode.AnyOf anyOf ->
                    combine(evaluateChildren(anyOf.children(), answers, atomEvaluations), Trilean::anyOf);
            case RuleNode.Not not -> {
                NodeResult child = evaluateNode(not.child(), answers, atomEvaluations);
                yield new NodeResult(child.value().not(), child.contributing());
            }
            case RuleNode.AtomRef atomRef -> evaluateAtom(atomRef, answers, atomEvaluations);
        };
    }

    private List<NodeResult> evaluateChildren(List<RuleNode> children, Answers answers,
                                               List<AtomEvaluation> atomEvaluations) {
        List<NodeResult> results = new ArrayList<>(children.size());
        for (RuleNode child : children) {
            results.add(evaluateNode(child, answers, atomEvaluations));
        }
        return results;
    }

    /**
     * all_of/any_of 공통 조합 — 값은 {@code combiner}(Trilean.allOf/anyOf)로 정하고, 그 값에
     * 기여한 원자는 "자기 값이 조합값과 같은 자식들"만 모은다. 흡수당한 자식(값이 다른 자식)은
     * 여기서 제외되며, 이 필터가 트리 매 단계 재귀 적용되므로 중첩 흡수도 그대로 걸러진다.
     */
    private NodeResult combine(List<NodeResult> children, Function<List<Trilean>, Trilean> combiner) {
        Trilean combined = combiner.apply(children.stream().map(NodeResult::value).toList());
        List<AtomEvaluation> contributing = children.stream()
                .filter(child -> child.value() == combined)
                .flatMap(child -> child.contributing().stream())
                .toList();
        return new NodeResult(combined, contributing);
    }

    private NodeResult evaluateAtom(RuleNode.AtomRef atomRef, Answers answers, List<AtomEvaluation> atomEvaluations) {
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
        AtomEvaluation evaluation = new AtomEvaluation(atomRef, outcome);
        atomEvaluations.add(evaluation);
        return new NodeResult(outcome.trilean(), List.of(evaluation));
    }
}
