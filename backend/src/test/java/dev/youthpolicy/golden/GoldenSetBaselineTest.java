package dev.youthpolicy.golden;

import dev.youthpolicy.api.RequestToAnswersMapper;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.evaluate.PolicyEvaluation;
import dev.youthpolicy.domain.evaluate.PolicyEvaluator;
import dev.youthpolicy.domain.evaluate.RuleEvaluator;
import dev.youthpolicy.domain.scope.OutOfScopeClassifier;
import dev.youthpolicy.domain.verdict.Verdict;
import dev.youthpolicy.policy.PolicyCatalog;
import dev.youthpolicy.policy.PolicyRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FP/FN baseline 측정 — PRD.md 5장 가드레일 지표(이슈 #17). {@link GoldenSetTest}가 케이스별로
 * "기대값=실제값"을 assert하는 것과 달리, 이 테스트는 골든셋 전체를 한 번에 집계해 "얼마나
 * 벗어났는지"를 수치(N·오분류 건수·비율)로 리포트한다.
 *
 * <p>PRD 5장 정의: FP(과대판정) = 골든셋이 ineligible인 케이스를 eligible로 오판정한 비율,
 * FN(과소판정) = 골든셋이 eligible인 케이스를 ineligible로 오판정한 비율. needs_review·
 * out_of_scope는 "가능/부적합" 단정이 아니므로 이 집계에서 제외한다(PRD 5장 원문).
 *
 * <p>FP는 PRD가 지정한 출시 게이트이므로 0건을 assert해 회귀를 차단한다. FN은 "측정·보고
 * 대상(게이트 아님)"이라 assert하지 않고 리포트만 한다. 골든셋 케이스가 바뀔 때마다(정책 추가·
 * 경계 확충 등) 이 리포트도 함께 갱신되므로 baseline 재측정이 항상 최신 골든셋 기준이다.
 */
class GoldenSetBaselineTest {

    private static final PolicyCatalog POLICY_CATALOG = new PolicyCatalog();
    private static final PolicyEvaluator POLICY_EVALUATOR =
            new PolicyEvaluator(new OutOfScopeClassifier(), new RuleEvaluator());
    private static final RequestToAnswersMapper ANSWERS_MAPPER = new RequestToAnswersMapper();

    @Test
    void reportsFalsePositiveAndFalseNegativeRate() throws IOException {
        List<GoldenSetTest.PolicyGoldenCase> cases = GoldenSetTest.cases();

        int ineligibleTotal = 0;
        int eligibleTotal = 0;
        int falsePositives = 0;
        int falseNegatives = 0;
        List<String> falsePositiveCases = new ArrayList<>();
        List<String> falseNegativeCases = new ArrayList<>();

        for (GoldenSetTest.PolicyGoldenCase policyCase : cases) {
            String policyId = policyCase.policyId();
            GoldenCase testCase = policyCase.testCase();
            PolicyRecord policy = POLICY_CATALOG.findById(policyId).orElseThrow();

            Answers answers = ANSWERS_MAPPER.toAnswers(testCase.answers());
            PolicyEvaluation evaluation =
                    POLICY_EVALUATOR.evaluate(policy.rule(), policy.outOfScopeGate(), answers);
            Verdict verdict = evaluation.verdict();
            String actualState = verdict.state().wireValue();
            String expectedState = testCase.expectedState();
            String label = policyId + ":" + testCase.caseId();

            if (expectedState.equals("ineligible")) {
                ineligibleTotal++;
                if (actualState.equals("eligible")) {
                    falsePositives++;
                    falsePositiveCases.add(label);
                }
            } else if (expectedState.equals("eligible")) {
                eligibleTotal++;
                if (actualState.equals("ineligible")) {
                    falseNegatives++;
                    falseNegativeCases.add(label);
                }
            }
        }

        double fpRate = ineligibleTotal == 0 ? 0.0 : (double) falsePositives / ineligibleTotal;
        double fnRate = eligibleTotal == 0 ? 0.0 : (double) falseNegatives / eligibleTotal;

        String report = String.format(
                "골든셋 FP/FN baseline — N=%d (ineligible=%d, eligible=%d, 그 외 needs_review/out_of_scope=%d) | "
                        + "FP=%d/%d(%.1f%%) FN=%d/%d(%.1f%%)",
                cases.size(), ineligibleTotal, eligibleTotal,
                cases.size() - ineligibleTotal - eligibleTotal,
                falsePositives, ineligibleTotal, fpRate * 100,
                falseNegatives, eligibleTotal, fnRate * 100);
        System.out.println(report);

        assertThat(falsePositives)
                .as("FP 상한선(PRD 5장 출시 게이트) 위반 — %s, 위반 케이스: %s", report, falsePositiveCases)
                .isZero();

        // FN은 PRD 5장 "측정·보고 대상(게이트 아님)" — assert하지 않고 리포트만 남긴다.
        if (!falseNegativeCases.isEmpty()) {
            System.out.println("FN 발생 케이스(보고용, 비차단): " + falseNegativeCases);
        }
    }
}
