package dev.youthpolicy.domain.kleene;

import java.util.List;

/**
 * Kleene 강(strong) 3치 논리 — DOMAIN.md §3.
 *
 * all_of(AND)는 FALSE가 흡수원소, any_of(OR)는 TRUE가 흡수원소다(§3.2/§3.3).
 * 의미의 원본은 DOMAIN §3이며, 이 타입은 그 정의를 그대로 코드화한다.
 */
public enum Trilean {
    TRUE,
    FALSE,
    UNKNOWN;

    /** DOMAIN §3.2 not 진리표. */
    public Trilean not() {
        return switch (this) {
            case TRUE -> FALSE;
            case FALSE -> TRUE;
            case UNKNOWN -> UNKNOWN;
        };
    }

    /**
     * DOMAIN §3.3 all_of n항 일반화 — 하나라도 FALSE면 FALSE.
     * 아니면 하나라도 UNKNOWN이면 UNKNOWN. 모두 TRUE면 TRUE.
     */
    public static Trilean allOf(List<Trilean> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("allOf는 최소 1개 이상의 값이 필요합니다.");
        }
        boolean hasUnknown = false;
        for (Trilean value : values) {
            if (value == FALSE) {
                return FALSE;
            }
            if (value == UNKNOWN) {
                hasUnknown = true;
            }
        }
        return hasUnknown ? UNKNOWN : TRUE;
    }

    /**
     * DOMAIN §3.3 any_of n항 일반화 — 하나라도 TRUE면 TRUE.
     * 아니면 하나라도 UNKNOWN이면 UNKNOWN. 모두 FALSE면 FALSE.
     */
    public static Trilean anyOf(List<Trilean> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("anyOf는 최소 1개 이상의 값이 필요합니다.");
        }
        boolean hasUnknown = false;
        for (Trilean value : values) {
            if (value == TRUE) {
                return TRUE;
            }
            if (value == UNKNOWN) {
                hasUnknown = true;
            }
        }
        return hasUnknown ? UNKNOWN : FALSE;
    }
}
