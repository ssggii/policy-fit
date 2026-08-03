package dev.youthpolicy.domain.atom;

import dev.youthpolicy.domain.atom.evaluator.AgeAtomEvaluator;
import dev.youthpolicy.domain.atom.evaluator.AssetSelfAtomEvaluator;
import dev.youthpolicy.domain.atom.evaluator.HouseholdCompositionAtomEvaluator;
import dev.youthpolicy.domain.atom.evaluator.HousingNoneAtomEvaluator;
import dev.youthpolicy.domain.atom.evaluator.IncomeSelfAtomEvaluator;
import dev.youthpolicy.domain.atom.evaluator.LeaseTypeAtomEvaluator;

import java.util.Map;
import java.util.Optional;

/**
 * 원자 카탈로그 — DOMAIN §2.3의 12개 원자 전부를 값 유형·일상어 라벨(F-004)에 매핑한다.
 * Evaluator는 지금까지 등록된 정책이 쓰는 6개(age, housing_none, lease_type, income_self, asset_self,
 * household_composition)만 등록한다. household_aggregate 원자는 OutOfScopeClassifier가 사전분류하고,
 * admin_discretion 원자는 RuleEvaluator가 값 유형만으로 항상 unknown을 반환하므로(§3.4)
 * 둘 다 evaluator가 필요 없다.
 *
 * 정적 유틸리티: 도메인 계층을 프레임워크(Spring) 의존 없이 유지하기 위해 상태 없는 static 메서드로 둔다.
 */
public final class AtomCatalog {

    private static final Map<AtomId, ValueType> VALUE_TYPES = Map.ofEntries(
            Map.entry(AtomId.AGE, ValueType.SELF),
            Map.entry(AtomId.HOUSING_NONE, ValueType.SELF),
            Map.entry(AtomId.LEASE_TYPE, ValueType.SELF),
            Map.entry(AtomId.INCOME_SELF, ValueType.SELF),
            Map.entry(AtomId.INCOME_HOUSEHOLD, ValueType.HOUSEHOLD_AGGREGATE),
            Map.entry(AtomId.INCOME_ORIGINAL, ValueType.HOUSEHOLD_AGGREGATE),
            Map.entry(AtomId.ASSET_SELF, ValueType.SELF),
            // household_composition: DOMAIN §2.3 각주 — 실제 값 유형은 정책 파라미터(scope)가 결정하며
            // "self 또는 household_aggregate"(§2.3)로 열려 있다. 청년월세 면제 게이트(§5.3·ADR-0005 D5)가
            // scope:self로만 쓰므로 SELF로 둔다. evaluator는 self만 지원하고 그 외 scope는 fail-loud한다
            // (ADR-0003 D4). scope 기반 값 유형 분기(aggregate)는 이슈 #9에서 확정한다.
            Map.entry(AtomId.HOUSEHOLD_COMPOSITION, ValueType.SELF),
            Map.entry(AtomId.EMPLOYMENT, ValueType.SELF),
            Map.entry(AtomId.EDUCATION, ValueType.SELF),
            Map.entry(AtomId.BENEFIT_OVERLAP, ValueType.SELF),
            Map.entry(AtomId.SEPARATE_RESIDENCE, ValueType.ADMIN_DISCRETION));

    private static final Map<AtomId, String> LABELS = Map.ofEntries(
            Map.entry(AtomId.AGE, "나이"),
            Map.entry(AtomId.HOUSING_NONE, "무주택 여부"),
            Map.entry(AtomId.LEASE_TYPE, "임차 형태"),
            Map.entry(AtomId.INCOME_SELF, "본인 소득"),
            Map.entry(AtomId.INCOME_HOUSEHOLD, "가구 소득"),
            Map.entry(AtomId.INCOME_ORIGINAL, "원가구 소득"),
            Map.entry(AtomId.ASSET_SELF, "본인 재산"),
            Map.entry(AtomId.HOUSEHOLD_COMPOSITION, "가구 구성"),
            Map.entry(AtomId.EMPLOYMENT, "재직 상태"),
            Map.entry(AtomId.EDUCATION, "학력"),
            Map.entry(AtomId.BENEFIT_OVERLAP, "중복수혜 여부"),
            Map.entry(AtomId.SEPARATE_RESIDENCE, "별도 거주 인정"));

    private static final Map<AtomId, AtomEvaluator> EVALUATORS = Map.of(
            AtomId.AGE, new AgeAtomEvaluator(),
            AtomId.HOUSING_NONE, new HousingNoneAtomEvaluator(),
            AtomId.LEASE_TYPE, new LeaseTypeAtomEvaluator(),
            AtomId.INCOME_SELF, new IncomeSelfAtomEvaluator(),
            AtomId.ASSET_SELF, new AssetSelfAtomEvaluator(),
            AtomId.HOUSEHOLD_COMPOSITION, new HouseholdCompositionAtomEvaluator());

    private AtomCatalog() {
    }

    public static ValueType valueTypeOf(AtomId atomId) {
        ValueType type = VALUE_TYPES.get(atomId);
        if (type == null) {
            throw new IllegalArgumentException("카탈로그에 등록되지 않은 원자입니다: " + atomId);
        }
        return type;
    }

    public static String labelOf(AtomId atomId) {
        return LABELS.getOrDefault(atomId, atomId.wireValue());
    }

    public static Optional<AtomEvaluator> evaluatorFor(AtomId atomId) {
        return Optional.ofNullable(EVALUATORS.get(atomId));
    }
}
