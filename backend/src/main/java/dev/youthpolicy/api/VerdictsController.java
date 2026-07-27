package dev.youthpolicy.api;

import dev.youthpolicy.api.dto.ApplicationDto;
import dev.youthpolicy.api.dto.AtomReasoningDto;
import dev.youthpolicy.api.dto.VerdictDto;
import dev.youthpolicy.api.dto.VerdictRequestDto;
import dev.youthpolicy.api.dto.VerdictResultDto;
import dev.youthpolicy.domain.atom.AtomCatalog;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.evaluate.AtomEvaluation;
import dev.youthpolicy.domain.evaluate.RuleEvaluationResult;
import dev.youthpolicy.domain.evaluate.RuleEvaluator;
import dev.youthpolicy.domain.scope.OutOfScopeClassifier;
import dev.youthpolicy.domain.verdict.Verdict;
import dev.youthpolicy.domain.verdict.VerdictMapper;
import dev.youthpolicy.domain.verdict.VerdictState;
import dev.youthpolicy.policy.PolicyApplication;
import dev.youthpolicy.policy.PolicyCatalog;
import dev.youthpolicy.policy.PolicyNotFoundException;
import dev.youthpolicy.policy.PolicyRecord;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * {@code POST /verdicts} — contracts/openapi.yaml. 무인증·stateless: 요청 answers는
 * 판정 계산에만 쓰이고 저장·로깅하지 않는다(미저장·무로깅 원칙, PRD 9장).
 *
 * 판정 경로는 순수 결정론 로직(원자 평가기 + Kleene 조합기 + out_of_scope 사전분류)만
 * 사용한다 — 런타임 LLM 없음.
 */
@RestController
public class VerdictsController {

    private final PolicyCatalog policyCatalog;
    private final OutOfScopeClassifier outOfScopeClassifier;
    private final RuleEvaluator ruleEvaluator;
    private final RequestToAnswersMapper requestToAnswersMapper;

    public VerdictsController(PolicyCatalog policyCatalog,
                               OutOfScopeClassifier outOfScopeClassifier,
                               RuleEvaluator ruleEvaluator,
                               RequestToAnswersMapper requestToAnswersMapper) {
        this.policyCatalog = policyCatalog;
        this.outOfScopeClassifier = outOfScopeClassifier;
        this.ruleEvaluator = ruleEvaluator;
        this.requestToAnswersMapper = requestToAnswersMapper;
    }

    @PostMapping("/verdicts")
    public ResponseEntity<VerdictResultDto> judge(@Valid @RequestBody VerdictRequestDto request) {
        PolicyRecord policy = policyCatalog.findById(request.policyId())
                .orElseThrow(() -> new PolicyNotFoundException(request.policyId()));

        // out_of_scope는 런타임 평가 이전의 정적 분류다(ADR-0003 D3) — 답변을 평가하지 않는다.
        if (outOfScopeClassifier.isOutOfScope(policy.rule())) {
            return ResponseEntity.ok(toDto(policy, Verdict.outOfScope(), List.of()));
        }

        Answers answers = requestToAnswersMapper.toAnswers(request.answers());
        RuleEvaluationResult result = ruleEvaluator.evaluate(policy.rule(), answers);
        Verdict verdict = VerdictMapper.toVerdict(result.value(), result.atomEvaluations());
        return ResponseEntity.ok(toDto(policy, verdict, result.atomEvaluations()));
    }

    private VerdictResultDto toDto(PolicyRecord policy, Verdict verdict, List<AtomEvaluation> atomEvaluations) {
        VerdictDto verdictDto = new VerdictDto(
                verdict.state().wireValue(),
                verdict.unknownReasons().isEmpty()
                        ? null
                        : verdict.unknownReasons().stream().map(r -> r.wireValue()).toList());

        List<AtomReasoningDto> reasoning = atomEvaluations.stream()
                .map(this::toReasoningDto)
                .toList();

        // F-006: '가능'·'추가 확인 필요'만 신청 채널 제공. '부적합'·'자가판정 불가'는 없음/생략.
        ApplicationDto application = isApplicationEligible(verdict.state())
                ? toApplicationDto(policy.application())
                : null;

        return new VerdictResultDto(policy.id(), verdictDto, reasoning, application);
    }

    private boolean isApplicationEligible(VerdictState state) {
        return state == VerdictState.ELIGIBLE || state == VerdictState.NEEDS_REVIEW;
    }

    private ApplicationDto toApplicationDto(PolicyApplication application) {
        if (application == null) {
            return null;
        }
        return new ApplicationDto(application.url(), application.selectionMethod(), application.period());
    }

    private AtomReasoningDto toReasoningDto(AtomEvaluation evaluation) {
        var atomRef = evaluation.atomRef();
        var outcome = evaluation.outcome();
        String result = switch (outcome.trilean()) {
            case TRUE -> "met";
            case FALSE -> "unmet";
            case UNKNOWN -> "unknown";
        };
        String source = atomRef.meta() != null ? atomRef.meta().source() : null;
        Integer year = atomRef.meta() != null ? atomRef.meta().year() : null;

        return new AtomReasoningDto(
                atomRef.atom().wireValue(),
                AtomCatalog.labelOf(atomRef.atom()),
                result,
                outcome.detail(),
                source,
                year);
    }
}
