package dev.youthpolicy.domain.verdict;

/**
 * 판정 결과 4-state — DOMAIN.md §1.1. 기계 계약은 contracts/verdict.schema.json.
 */
public enum VerdictState {
    ELIGIBLE("eligible"),
    INELIGIBLE("ineligible"),
    NEEDS_REVIEW("needs_review"),
    OUT_OF_SCOPE("out_of_scope");

    private final String wireValue;

    VerdictState(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
