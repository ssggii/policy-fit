package dev.youthpolicy.domain.scope;

import dev.youthpolicy.domain.atom.AtomId;
import dev.youthpolicy.domain.rule.RuleNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * household_aggregate 원자(예: income_household)를 참조하는 합성 규칙이 out_of_scope로
 * 분류되는지 확인한다. income_household/income_original에는 evaluator가 등록돼 있지 않은데도
 * 분류만은 성립해야 한다 — 엔진이 원자 카탈로그의 evaluator 완결성에 의존하지 않음을 증명한다.
 */
class OutOfScopeClassifierTest {

    private final OutOfScopeClassifier classifier = new OutOfScopeClassifier();

    @Test
    void allSelfAtomsAreInScope() {
        RuleNode rule = new RuleNode.AllOf(List.of(
                atomRef(AtomId.AGE),
                atomRef(AtomId.HOUSING_NONE),
                atomRef(AtomId.INCOME_SELF)));

        assertThat(classifier.isOutOfScope(rule)).isFalse();
    }

    @Test
    void householdAggregateAtomNestedInAnyOfMarksOutOfScope() {
        RuleNode rule = new RuleNode.AllOf(List.of(
                atomRef(AtomId.AGE),
                new RuleNode.AnyOf(List.of(
                        atomRef(AtomId.HOUSING_NONE),
                        atomRef(AtomId.INCOME_HOUSEHOLD)))));

        assertThat(classifier.isOutOfScope(rule)).isTrue();
    }

    @Test
    void householdAggregateAtomUnderNotIsStillOutOfScope() {
        RuleNode rule = new RuleNode.Not(atomRef(AtomId.INCOME_ORIGINAL));

        assertThat(classifier.isOutOfScope(rule)).isTrue();
    }

    @Test
    void adminDiscretionAtomAloneDoesNotMarkOutOfScope() {
        RuleNode rule = atomRef(AtomId.SEPARATE_RESIDENCE);

        assertThat(classifier.isOutOfScope(rule)).isFalse();
    }

    private RuleNode.AtomRef atomRef(AtomId atomId) {
        return new RuleNode.AtomRef(atomId, Map.of(), null);
    }
}
