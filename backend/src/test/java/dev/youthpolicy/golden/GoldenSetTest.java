package dev.youthpolicy.golden;

import dev.youthpolicy.api.RequestToAnswersMapper;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.evaluate.RuleEvaluationResult;
import dev.youthpolicy.domain.evaluate.RuleEvaluator;
import dev.youthpolicy.domain.scope.OutOfScopeClassifier;
import dev.youthpolicy.domain.verdict.Verdict;
import dev.youthpolicy.domain.verdict.VerdictMapper;
import dev.youthpolicy.policy.PolicyCatalog;
import dev.youthpolicy.policy.PolicyRecord;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 골든셋 — {@code src/test/resources/golden/<policy_id>.json}. PolicyCatalog(정책 JSON) →
 * OutOfScopeClassifier → RequestToAnswersMapper → RuleEvaluator → VerdictMapper까지, HTTP 계층을
 * 제외한 판정 파이프라인 전체를 정책별·케이스별로 검증한다(HTTP/JSON 직렬화 자체는
 * VerdictsControllerTest가 별도로 담당).
 *
 * {@link #POLICY_IDS}에 등록된 모든 정책의 골든셋을 이 하나의 테스트 클래스로 커버한다 — 정책이
 * 늘어도 별도 테스트 클래스를 만들지 않고 이 목록만 확장한다(구조가 jutaek-dream과 동일한
 * "전부 self + all_of" 정책이라면 재사용 가능).
 */
class GoldenSetTest {

    /** 골든셋을 갖춘 정책 ID 목록 — 정책 추가마다 이 목록만 확장하면 된다. */
    private static final List<String> POLICY_IDS = List.of("jutaek-dream", "beotimmok-jeonse");

    private static final PolicyCatalog POLICY_CATALOG = new PolicyCatalog();
    private static final OutOfScopeClassifier OUT_OF_SCOPE_CLASSIFIER = new OutOfScopeClassifier();
    private static final RuleEvaluator RULE_EVALUATOR = new RuleEvaluator();
    private static final RequestToAnswersMapper ANSWERS_MAPPER = new RequestToAnswersMapper();

    static List<PolicyGoldenCase> cases() throws IOException {
        JsonMapper mapper = JsonMapper.builder().build();
        List<PolicyGoldenCase> allCases = new ArrayList<>();
        for (String policyId : POLICY_IDS) {
            String resource = "golden/" + policyId + ".json";
            try (InputStream in = GoldenSetTest.class.getClassLoader().getResourceAsStream(resource)) {
                if (in == null) {
                    throw new IllegalStateException("golden set 리소스를 찾을 수 없습니다: " + resource);
                }
                List<GoldenCase> policyCases = mapper.readValue(in, new TypeReference<List<GoldenCase>>() {
                });
                for (GoldenCase testCase : policyCases) {
                    allCases.add(new PolicyGoldenCase(policyId, testCase));
                }
            }
        }
        return allCases;
    }

    @ParameterizedTest
    @MethodSource("cases")
    void goldenCase(PolicyGoldenCase policyCase) {
        String policyId = policyCase.policyId();
        GoldenCase testCase = policyCase.testCase();

        PolicyRecord policy = POLICY_CATALOG.findById(policyId).orElseThrow();
        assertThat(OUT_OF_SCOPE_CLASSIFIER.isOutOfScope(policy.rule()))
                .as("%s 정책은 전부 self 원자라 out_of_scope가 아니어야 한다", policyId)
                .isFalse();

        Answers answers = ANSWERS_MAPPER.toAnswers(testCase.answers());
        RuleEvaluationResult result = RULE_EVALUATOR.evaluate(policy.rule(), answers);
        Verdict verdict = VerdictMapper.toVerdict(result.value(), result.contributingEvaluations());

        assertThat(verdict.state().wireValue())
                .as("policy=%s case=%s source=%s", policyId, testCase.caseId(), testCase.source())
                .isEqualTo(testCase.expectedState());

        if (testCase.expectedUnknownReasons() != null) {
            List<String> actualReasons = verdict.unknownReasons().stream()
                    .map(reason -> reason.wireValue())
                    .toList();
            assertThat(actualReasons)
                    .as("policy=%s case=%s", policyId, testCase.caseId())
                    .containsExactlyInAnyOrderElementsOf(testCase.expectedUnknownReasons());
        }
    }

    /** 정책 ID + 그 정책 골든셋의 케이스 1건. 테스트 이름에 정책 ID가 보이도록 toString을 오버라이드한다. */
    record PolicyGoldenCase(String policyId, GoldenCase testCase) {
        @Override
        public String toString() {
            return policyId + ":" + testCase.caseId();
        }
    }
}
