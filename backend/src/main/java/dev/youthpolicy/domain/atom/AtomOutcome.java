package dev.youthpolicy.domain.atom;

import dev.youthpolicy.domain.kleene.Trilean;
import dev.youthpolicy.domain.verdict.UnknownReason;

import java.util.Objects;

/**
 * 원자 하나의 평가 결과 — Trilean 값 + (UNKNOWN일 때만) 출처 태그(§3.1) + 사람이 읽는 근거 문구.
 * compact constructor가 "UNKNOWN이면, 그리고 그때만 unknownReason이 있다"를 강제한다.
 */
public record AtomOutcome(Trilean trilean, UnknownReason unknownReason, String detail) {

    public AtomOutcome {
        Objects.requireNonNull(trilean, "trilean은 필수입니다.");
        Objects.requireNonNull(detail, "detail은 필수입니다.");
        if (trilean == Trilean.UNKNOWN) {
            Objects.requireNonNull(unknownReason, "trilean이 UNKNOWN이면 unknownReason이 필요합니다.");
        } else if (unknownReason != null) {
            throw new IllegalArgumentException("trilean이 UNKNOWN이 아니면 unknownReason은 null이어야 합니다.");
        }
    }

    /** TRUE 또는 FALSE 결과 생성. UNKNOWN을 넘기면 compact constructor가 실패한다(unknown()을 쓸 것). */
    public static AtomOutcome of(Trilean trilean, String detail) {
        return new AtomOutcome(trilean, null, detail);
    }

    public static AtomOutcome unknown(UnknownReason reason, String detail) {
        return new AtomOutcome(Trilean.UNKNOWN, reason, detail);
    }
}
