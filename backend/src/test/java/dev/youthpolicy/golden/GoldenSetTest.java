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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 청년 주택드림 청약통장 골든셋 — src/test/resources/golden/jutaek-dream.json.
 * PolicyCatalog(정책 JSON) → OutOfScopeClassifier → RequestToAnswersMapper → RuleEvaluator →
 * VerdictMapper까지, HTTP 계층을 제외한 판정 파이프라인 전체를 케이스별로 검증한다
 * (HTTP/JSON 직렬화 자체는 VerdictsControllerTest가 별도로 담당).
 */
class GoldenSetTest {

    private static final PolicyCatalog POLICY_CATALOG = new PolicyCatalog();
    private static final OutOfScopeClassifier OUT_OF_SCOPE_CLASSIFIER = new OutOfScopeClassifier();
    private static final RuleEvaluator RULE_EVALUATOR = new RuleEvaluator();
    private static final RequestToAnswersMapper ANSWERS_MAPPER = new RequestToAnswersMapper();

    static List<GoldenCase> cases() throws IOException {
        JsonMapper mapper = JsonMapper.builder().build();
        try (InputStream in = GoldenSetTest.class.getClassLoader().getResourceAsStream("golden/jutaek-dream.json")) {
            if (in == null) {
                throw new IllegalStateException("golden set 리소스를 찾을 수 없습니다: golden/jutaek-dream.json");
            }
            return mapper.readValue(in, new TypeReference<List<GoldenCase>>() {
            });
        }
    }

    @ParameterizedTest
    @MethodSource("cases")
    void goldenCase(GoldenCase testCase) {
        PolicyRecord policy = POLICY_CATALOG.findById("jutaek-dream").orElseThrow();
        assertThat(OUT_OF_SCOPE_CLASSIFIER.isOutOfScope(policy.rule()))
                .as("jutaek-dream 정책은 전부 self 원자라 out_of_scope가 아니어야 한다")
                .isFalse();

        Answers answers = ANSWERS_MAPPER.toAnswers(testCase.answers());
        RuleEvaluationResult result = RULE_EVALUATOR.evaluate(policy.rule(), answers);
        Verdict verdict = VerdictMapper.toVerdict(result.value(), result.atomEvaluations());

        assertThat(verdict.state().wireValue())
                .as("case=%s source=%s", testCase.caseId(), testCase.source())
                .isEqualTo(testCase.expectedState());

        if (testCase.expectedUnknownReasons() != null) {
            List<String> actualReasons = verdict.unknownReasons().stream()
                    .map(reason -> reason.wireValue())
                    .toList();
            assertThat(actualReasons)
                    .as("case=%s", testCase.caseId())
                    .containsExactlyInAnyOrderElementsOf(testCase.expectedUnknownReasons());
        }
    }
}
