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
 * {@code asset_self} 원자 — DOMAIN §2.3·§4.3: params {max_krw}(자산 상한) 이하 여부.
 * "self(느슨)" 값 유형(§2.3) — 모름 허용 → unknown.
 *
 * <p>Phase 7 잠정 규칙(IncomeSelfAtomEvaluator와 동일): {@code approx=true}인 응답은 값이 있어도
 * 항상 UNKNOWN으로 처리하고 true/false로 자동 해소하지 않는다. 태그는 {@code input_uncertain}이다.
 */
public final class AssetSelfAtomEvaluator implements AtomEvaluator {

    @Override
    public AtomOutcome evaluate(Map<String, Object> params, Answers answers) {
        AnswerApproxInt asset = answers == null ? null : answers.assetSelfKrw();
        if (asset == null || !asset.known() || asset.value() == null) {
            return AtomOutcome.unknown(UnknownReason.INPUT_UNCERTAIN, "본인 재산을 확인할 수 없습니다.");
        }
        if (asset.approx()) {
            return AtomOutcome.unknown(UnknownReason.INPUT_UNCERTAIN, "대략적인 재산액만 입력되어 정확한 판정이 불가합니다.");
        }

        // fail-closed: max_krw 누락은 규칙 데이터 오류다. 조용히 TRUE로 통과시키면 자산 요건이
        // 무력화된 채 판정되므로, 정책 JSON이 잘못됐다는 신호로 즉시 실패시킨다.
        long maxKrw = ParamsUtil.getLong(params, "max_krw")
                .orElseThrow(() -> new IllegalStateException("asset_self 원자에 max_krw 파라미터가 없습니다."));

        if (asset.value() > maxKrw) {
            return AtomOutcome.of(Trilean.FALSE, "본인 재산이 기준을 초과합니다.");
        }
        return AtomOutcome.of(Trilean.TRUE, "본인 재산이 기준 이하입니다.");
    }
}
