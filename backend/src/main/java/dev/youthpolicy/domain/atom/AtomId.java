package dev.youthpolicy.domain.atom;

import java.util.Arrays;
import java.util.Optional;

/**
 * 요건 원자 ID — DOMAIN.md §2.3, contracts/rule-dsl.schema.json atom enum과 1:1.
 * 영구·append-only — 폐기돼도 재사용 금지(§2.4).
 */
public enum AtomId {
    AGE("age"),
    HOUSING_NONE("housing_none"),
    LEASE_TYPE("lease_type"),
    INCOME_SELF("income_self"),
    INCOME_HOUSEHOLD("income_household"),
    INCOME_ORIGINAL("income_original"),
    ASSET_SELF("asset_self"),
    HOUSEHOLD_COMPOSITION("household_composition"),
    EMPLOYMENT("employment"),
    EDUCATION("education"),
    BENEFIT_OVERLAP("benefit_overlap"),
    SEPARATE_RESIDENCE("separate_residence");

    private final String wireValue;

    AtomId(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }

    public static Optional<AtomId> fromWireValue(String wireValue) {
        return Arrays.stream(values()).filter(a -> a.wireValue.equals(wireValue)).findFirst();
    }
}
