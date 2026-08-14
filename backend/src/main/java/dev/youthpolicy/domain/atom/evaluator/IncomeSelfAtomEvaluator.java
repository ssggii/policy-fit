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
 * <p><b>환산 규정 확정 결과(이슈 #14, 2026-08-14).</b> API 요청 필드는 세전 월소득
 * ({@code income_self_monthly_krw})이고 이 구현은 {@code ×12}로 연 환산한다. 3종 정책의 소득 정의를
 * 1차 출처로 대조한 결과, <b>이 환산은 세 정책 중 두 곳에서 정책이 보는 것과 다른 값을 만든다.</b>
 *
 * <ul>
 *   <li><b>주택드림</b> — "직전년도 또는 전전년도 신고소득이 있는 자로 연소득 5천만 원 이하"
 *       (주택도시기금 상품안내). 기준이 <b>과거 확정 신고소득</b>이라 현재 월소득 환산값과 시점이
 *       다르다. 올해 취업자는 실제 신고소득이 0에 가까운데 환산값은 그렇지 않고, 작년 고소득 후
 *       퇴사자는 그 반대다. 경계대역 오차가 아니라 다른 값을 비교하는 것이다.</li>
 *   <li><b>버팀목</b> — "합산 총소득이 5천만원 이하", 산정은 "현재 재직 중인 직장 또는 현재 영위
 *       중인 사업을 기준"(주택도시기금 대출안내). 시점은 현재가 맞다. 다만 소득 증빙이 "소득금액
 *       증명원 또는 … 연말정산용 원천징수영수증 …, 급여내역이 포함된 증명서 … 중 택1"(같은 사이트
 *       이용절차 및 제출서류)이라 <b>연간 확정액 경로와 급여내역 경로가 모두 열려 있다.</b> 전자가
 *       적용되면 상여·비정기 소득이 포함돼 ×12보다 커지고 후자면 가까워지는데, 신청자에게 어느
 *       쪽이 적용될지는 판정 시점에 알 수 없다. 5천만 경계 근처에서만 판정이 뒤집힌다.</li>
 *   <li><b>청년월세</b> — 소득평가액이 월 단위 기준 중위소득 60% 대비이고, {@code max_krw}는 이를
 *       세전 연액으로 역산해둔 값이다(이슈 #15, 정책 JSON meta 참조). ×12 후 실질적으로 ÷12 되므로
 *       <b>유일하게 정합한다.</b></li>
 * </ul>
 *
 * <p>정확도를 실제로 올리려면 입력 필드 자체가 바뀌어야 하고 그것은 {@code contracts/openapi.yaml}
 * 변경(등급 C)이라 <b>이슈 #82로 분리</b>했다. 이 구현은 그때까지 위 한계를 안은 채 동작한다 —
 * 미확정 가정이 아니라 <b>확인된 한계</b>다.</p>
 *
 * <p>1차 출처(2026-08-14 대조, ADR-0008 tier 1): 주택드림
 * {@code nhuf.molit.go.kr/FP/FP07/FP0701/FP07010301.jsp} · 버팀목 대출안내
 * {@code /FP/FP05/FP0502/FP05020301.jsp}, 제출서류 {@code /FP05020302.jsp} · 청년월세는 복지로
 * {@code WLF00004661}(이슈 #15). 이 인용문들은 <b>정책 레코드의 {@code meta.source}에 넣지 않았다</b> —
 * ADR-0008 D2가 {@code meta.source}를 사용자 노출용(F-004)으로, 검증 시점·한계·이슈 참조를 내부
 * {@code provenance} 형제 필드로 분리했고 그 도입은 이슈 #76이기 때문이다. #76이 여기 인용문을
 * {@code provenance.quote}로 옮긴다.</p>
 *
 * <p>미확인으로 남은 규정: "재직 1년 미만은 급여내역서 총액을 연소득으로 환산"이라는 서술이
 * 검색 요약에 나오지만 tier 1 원문을 확보하지 못했다. ADR-0008 D7에 따라 기록하지 않는다.</p>
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

        // 월→연 환산: ×12. 청년월세에만 정합하고 주택드림·버팀목과는 어긋난다(이슈 #14 확정, #82에서 정정).
        // 위 클래스 주석의 정책별 대조표 참조.
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
