package dev.youthpolicy.domain.atom.evaluator;

import dev.youthpolicy.domain.atom.AnswerApproxInt;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.atom.AtomEvaluator;
import dev.youthpolicy.domain.atom.AtomOutcome;
import dev.youthpolicy.domain.atom.ParamsUtil;
import dev.youthpolicy.domain.kleene.Trilean;
import dev.youthpolicy.domain.verdict.UnknownReason;

import java.util.Map;

/**
 * {@code income_self} 원자 — DOMAIN §2.3·§4.3: params {max_krw}(연소득 상한) 이하 여부.
 *
 * <p>잠정 가정(Phase 7 미확정, TODO): API 요청 필드는 월소득({@code income_self_monthly_krw})이고,
 * 월→연 환산 공식이 아직 공식 출처로 확정되지 않았다. 이 구현은 단순 {@code ×12}로 근사한다.
 * 실제 종합소득/근로소득 산정 방식(비과세 항목·4대보험 공제 등)은 확정되는 대로 갱신해야 한다.
 *
 * <p>Phase 7 잠정 규칙: {@code approx=true}인 응답은 값이 있어도 항상 UNKNOWN으로 처리하고
 * true/false로 자동 해소하지 않는다. 태그는 {@code input_uncertain}이다 — DOMAIN §3.4는 self 원자의
 * '모름/대략' 입력을 함께 입력 불확실로 분류한다({@code boundary}는 §3.1상 "임계 근처 대략값"에
 * 대응하는 별도 출처로, 실제 임계값과의 근접도 판정 로직이 있어야 성립하며 이 구현엔 없다).
 */
public final class IncomeSelfAtomEvaluator implements AtomEvaluator {

    private static final int MONTHS_PER_YEAR = 12;

    @Override
    public AtomOutcome evaluate(Map<String, Object> params, Answers answers) {
        AnswerApproxInt income = answers == null ? null : answers.incomeSelfMonthlyKrw();
        if (income == null || !income.known() || income.value() == null) {
            return AtomOutcome.unknown(UnknownReason.INPUT_UNCERTAIN, "본인 소득을 확인할 수 없습니다.");
        }
        if (income.approx()) {
            return AtomOutcome.unknown(UnknownReason.INPUT_UNCERTAIN, "대략적인 소득만 입력되어 정확한 판정이 불가합니다.");
        }

        // 월→연 환산: 단순 ×12 (잠정 가정 — 공식 환산 규정 미확정, 위 클래스 주석 참조)
        long annualIncomeKrw = (long) income.value() * MONTHS_PER_YEAR;
        // fail-closed: max_krw 누락은 규칙 데이터 오류다. 조용히 TRUE로 통과시키면 소득 요건이
        // 무력화된 채 판정되므로, 정책 JSON이 잘못됐다는 신호로 즉시 실패시킨다.
        long maxKrw = ParamsUtil.getLong(params, "max_krw")
                .orElseThrow(() -> new IllegalStateException("income_self 원자에 max_krw 파라미터가 없습니다."));

        if (annualIncomeKrw > maxKrw) {
            return AtomOutcome.of(Trilean.FALSE, "연소득 환산액이 기준을 초과합니다.");
        }
        return AtomOutcome.of(Trilean.TRUE, "연소득 환산액이 기준 이하입니다.");
    }
}
