package dev.youthpolicy.domain.atom.evaluator;

import dev.youthpolicy.domain.atom.AnswerBool;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.atom.AtomEvaluator;
import dev.youthpolicy.domain.atom.AtomOutcome;
import dev.youthpolicy.domain.kleene.Trilean;
import dev.youthpolicy.domain.verdict.UnknownReason;

import java.util.Map;

/**
 * {@code housing_none} 원자 — DOMAIN §2.3·§4.3: 파라미터 없음, 무주택(true) 요구.
 */
public final class HousingNoneAtomEvaluator implements AtomEvaluator {

    @Override
    public AtomOutcome evaluate(Map<String, Object> params, Answers answers) {
        AnswerBool housingNone = answers == null ? null : answers.housingNone();
        if (housingNone == null || !housingNone.known() || housingNone.value() == null) {
            return AtomOutcome.unknown(UnknownReason.INPUT_UNCERTAIN, "무주택 여부를 확인할 수 없습니다.");
        }

        boolean isHousingNone = housingNone.value();
        if (isHousingNone) {
            return AtomOutcome.of(Trilean.TRUE, "무주택입니다.");
        }
        return AtomOutcome.of(Trilean.FALSE, "주택을 소유하고 있어 무주택 요건을 충족하지 않습니다.");
    }
}
