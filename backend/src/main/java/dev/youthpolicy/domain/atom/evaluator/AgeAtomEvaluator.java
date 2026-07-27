package dev.youthpolicy.domain.atom.evaluator;

import dev.youthpolicy.domain.atom.AnswerInt;
import dev.youthpolicy.domain.atom.Answers;
import dev.youthpolicy.domain.atom.AtomEvaluator;
import dev.youthpolicy.domain.atom.AtomOutcome;
import dev.youthpolicy.domain.atom.ParamsUtil;
import dev.youthpolicy.domain.kleene.Trilean;
import dev.youthpolicy.domain.verdict.UnknownReason;

import java.util.Map;

/**
 * {@code age} 원자 — DOMAIN §2.3·§4.3: params {min?, max?}(만 나이) 포함 여부.
 */
public final class AgeAtomEvaluator implements AtomEvaluator {

    @Override
    public AtomOutcome evaluate(Map<String, Object> params, Answers answers) {
        AnswerInt age = answers == null ? null : answers.age();
        if (age == null || !age.known() || age.value() == null) {
            return AtomOutcome.unknown(UnknownReason.INPUT_UNCERTAIN, "나이를 확인할 수 없습니다.");
        }

        int value = age.value();
        Long min = ParamsUtil.getLong(params, "min").orElse(null);
        Long max = ParamsUtil.getLong(params, "max").orElse(null);
        boolean withinMin = min == null || value >= min;
        boolean withinMax = max == null || value <= max;

        if (withinMin && withinMax) {
            return AtomOutcome.of(Trilean.TRUE, "만 " + value + "세는 허용 나이 범위 안입니다.");
        }
        return AtomOutcome.of(Trilean.FALSE, "만 " + value + "세는 허용 나이 범위 밖입니다.");
    }
}
