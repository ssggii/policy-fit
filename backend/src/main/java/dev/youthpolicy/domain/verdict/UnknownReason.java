package dev.youthpolicy.domain.verdict;

/**
 * unknown 출처 태그 — DOMAIN.md §3.1. needs_review 안내 문구 분기용.
 */
public enum UnknownReason {
    INPUT_UNCERTAIN("input_uncertain"),
    ADMIN_DISCRETION("admin_discretion"),
    BOUNDARY("boundary");

    private final String wireValue;

    UnknownReason(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
