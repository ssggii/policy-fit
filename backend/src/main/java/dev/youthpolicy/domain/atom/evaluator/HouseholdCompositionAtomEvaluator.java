package dev.youthpolicy.domain.atom.evaluator;

import dev.youthpolicy.domain.atom.AnswerBool;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.atom.AtomEvaluator;
import dev.youthpolicy.domain.atom.AtomOutcome;
import dev.youthpolicy.domain.atom.ParamsUtil;
import dev.youthpolicy.domain.kleene.Trilean;
import dev.youthpolicy.domain.verdict.UnknownReason;

import java.util.Map;

/**
 * {@code household_composition} 원자 — DOMAIN §2.3·§4.3: params {scope, married?}.
 *
 * <p>이 슬라이스는 {@code scope:"self"} 경로만 지원한다 — 청년월세 면제 게이트(ADR-0005 D5)의
 * {@code {scope:"self", married:true}} 사용이 그 대상이다. {@code married:true} 파라미터는 "본인이
 * 혼인 상태인지"를 자가판정으로 묻는다.
 *
 * <p>{@code scope}가 {@code self}가 아니거나(가구 합산이 필요한 경우) 누락되면 fail-loud로 예외를
 * 던진다(ADR-0003 D4). {@code household_composition}의 값 유형은 §2.3 각주상 scope에 따라
 * self/household_aggregate로 갈리는 열린 문제이며(이슈 #9), 미확정 scope를 조용히 평가해
 * out_of_scope를 누락하는 실패 모드를 막기 위함이다.
 */
public final class HouseholdCompositionAtomEvaluator implements AtomEvaluator {

    @Override
    public AtomOutcome evaluate(Map<String, Object> params, Answers answers) {
        String scope = ParamsUtil.getString(params, "scope").orElse(null);
        if (!"self".equals(scope)) {
            // ADR-0003 D4 fail-loud — self 외 scope(household_aggregate 등)는 이슈 #9 확정 전까지 평가 불가.
            throw new UnsupportedOperationException(
                    "household_composition scope='" + scope + "'는 아직 평가할 수 없습니다 (self만 지원, 이슈 #9).");
        }

        // scope:self의 유일한 MVP 사용은 married 요건이다. married 파라미터가 없으면 규칙 데이터 오류로
        // 간주해 fail-closed(income_self의 max_krw 누락 처리와 동일 원칙).
        boolean requiredMarried = ParamsUtil.getBoolean(params, "married")
                .orElseThrow(() -> new IllegalStateException(
                        "household_composition{scope:self} 원자에 married 파라미터가 없습니다."));

        AnswerBool married = answers == null ? null : answers.married();
        if (married == null || !married.known() || married.value() == null) {
            return AtomOutcome.unknown(UnknownReason.INPUT_UNCERTAIN, "혼인 여부를 확인할 수 없습니다.");
        }

        if (married.value() == requiredMarried) {
            return AtomOutcome.of(Trilean.TRUE, requiredMarried
                    ? "혼인 상태로 요건을 충족합니다."
                    : "미혼으로 요건을 충족합니다.");
        }
        return AtomOutcome.of(Trilean.FALSE, requiredMarried
                ? "미혼이라 혼인 요건을 충족하지 않습니다."
                : "혼인 상태라 미혼 요건을 충족하지 않습니다.");
    }
}
