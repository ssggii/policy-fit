# DOMAIN — 판정 도메인 의미론

> 이 프로젝트에서 가장 정밀해야 하는 문서. 기능 변경과 무관하게 도메인 규칙(정책 개정 등)이 바뀔 때만 갱신.
> 서브도메인이 2개가 되면 domain/ 디렉토리로 분리.
> 상태: §0~5 작성 완료 (전체 rule 트리·임계값은 Phase 7 확정). 구조적 결정의 근거는 ADR-0003(판정 도메인 모델).

## 0. 개요 — 판정 파이프라인

판정은 하나의 파이프라인이다. 아래 각 절은 그 부품이며, "왜 이 구조인가"의 근거는 ADR-0003에 있다.

```
정책 ──자격 요건을 Rule DSL 트리로 표현(§4)──▶
   잎(leaf) = 요건 원자(§2) → 각 원자는 사용자 답으로 true/false/unknown 반환
   ──트리를 Kleene 3치 논리로 계산(§3)──▶ true/false/unknown
   ──4-state로 사상(§1)──▶ Verdict (가능/부적합/추가 확인 필요/범위 밖)
```

| 절 | 무엇 | 파이프라인 역할 |
|---|---|---|
| §1 Verdict | 결과 4-state의 의미 | 종착점 — 계산 결과를 사용자 결과로 |
| §2 요건 원자 | 재사용 조건 판정기 + 값 유형 | 트리의 잎 |
| §3 3치 논리 | unknown 포함 조합 규칙 | 트리 계산 방식 |
| §4 Rule DSL | 정책을 데이터로 적는 문법 | 나머지를 엮는 조립 |

**관통 개념 — 값 유형(§2.2):** 원자의 값 유형(self / household_aggregate / admin_discretion)이 '부적합'과 'out_of_scope'를 가르고 unknown의 전파를 좌우한다. §1·§2·§3을 잇는 축이다. (값·임계값은 각 절과 정책 규칙이 소유 — 이 개요는 구조만 담는다.)

## 1. Verdict 의미론

판정 결과는 4-state enum이다. 기계 계약은 `contracts/verdict.schema.json`, 의미의 원본은 이 절이다.

전제: **"가능"은 신청 자격 요건 충족을 뜻하며, 선발·수혜(추첨·선착순·예산 소진)를 보장하지 않는다** (PRD 0장 서비스 정의).

### 1.1 4가지 상태

| enum(제안) | 표시명 | 의미 | locus |
|---|---|---|---|
| `eligible` | 가능(적합) | 요건 충족 → 신청 권장 | 사용자 |
| `ineligible` | 부적합(해당 안 됨) | 요건 미충족 → 해당되지 않음 | 사용자 |
| `needs_review` | 추가 확인 필요(불확실) | 입력값 '모름/대략' 등으로 경계에 걸쳐 단정 불가 | 사용자(입력 불확실) |
| `out_of_scope` | 자가판정 불가(범위 밖) | 가구합산 등 자가 입력으로 확인 불가능한 요건을 가져 시스템이 자가판정할 수 없는 정책 | 시스템 |

*(enum 영문 키는 4e contracts freeze에서 최종 확정 — 위는 제안값)*

### 1.2 두 축의 구분 — 자격 결과 vs 범위 판정

- 앞 3개(가능·부적합·추가 확인 필요)는 **사용자 자격 결과**로, 요건 원자 평가에서 나온다.
- `out_of_scope`는 자격 결과가 아니라 **정책 범위 판정**이다. 정책이 자가판정 불가 원자를 포함하는지에 따라 *입력 평가 이전에* 결정된다. '부적합'과 혼동 금지 — 전자는 시스템 한계, 후자는 사용자 자격 결과 (SPEC F-003·F-007).

### 1.3 원자 평가 → verdict 사상

정책의 자격 요건은 요건 원자(2장)의 AND/OR 조합이며, 각 원자는 3치 논리(3장) 값 {true, false, unknown}을 갖는다. 조합 평가 결과를 자격 결과로 사상한다:

| 조합 평가 | verdict |
|---|---|
| true | 가능 (`eligible`) |
| false | 부적합 (`ineligible`) |
| unknown | 추가 확인 필요 (`needs_review`) |

단, 정책이 자가판정 불가 원자를 포함하면 위 사상에 앞서 `out_of_scope`로 분류한다(1.2). **예외:** 임계가 느슨해 대부분 충족하는 원자(예: 본인 재산)는 '모름'이어도 `out_of_scope`로 빼지 않고 unknown으로 두어 `needs_review`로 진행한다(SPEC F-007). 어떤 원자가 이 예외인지는 원자별 spec(2장)이 지정한다.

## 2. 요건 원자 (Requirement Atoms)

### 2.1 원자 모델

요건 원자는 **재사용 가능한 평가기**다. 정책마다 새로 짜지 않고, 정책 규칙(4장)이 파라미터(임계값 등)를 주입해 재사용한다. 정책 추가 = 코드가 아니라 규칙 데이터 한 건(PRD 6장 설계 원칙).

- 원자는 사용자 입력 변수와 정책 파라미터를 받아 3치 논리 값 {true, false, unknown}을 반환한다(3장).
- 원자는 정책을 모른다. 정책이 원자를 AND/OR로 조합하고 파라미터를 준다.
- **입력 변수 ≠ 원자.** 예: '세대 독립성'은 사용자에게 묻는 입력 변수(SPEC F-001)이고, 그 값은 `household_composition` 원자와 소득 원자의 scope 선택에 쓰인다.

### 2.2 값 유형 — 판정 가능성 분류

원자의 값 유형이 verdict 사상(1.2·1.3)을 좌우한다.

| 값 유형 | 뜻 | 자가입력 | verdict 영향 |
|---|---|---|---|
| `self` | 본인이 답할 수 있는 값 | 가능 | 정상 평가 (모름 → unknown) |
| `household_aggregate` | 가구·원가구·부부 합산이 필요한 값 | 불가 | 이 원자를 요구하는 정책은 `out_of_scope`로 분류(1.2) |
| `admin_discretion` | 행정청 재량 판단 값 (예: "시군구청장이 별도 거주 인정") | 불가 | 원자는 unknown 반환. OR로 자가판정 가능한 대체 경로가 있으면 해소, 없으면 needs_review |

핵심: `household_aggregate`와 `admin_discretion`은 **둘 다 자가입력 불가지만 처리가 다르다** — 전자는 정책 전체를 범위 밖으로(구조적 한계), 후자는 원자만 unknown으로 두고 Kleene 조합(3장)에 맡긴다(대체 경로 여지). 이 구분이 청년월세 면제군을 판정 대상으로 유지하는 근거다.

### 2.3 원자 카탈로그 (MVP)

| ID | 이름 | 입력 변수 | 값 유형 | 평가 개요 |
|---|---|---|---|---|
| `age` | 나이 | 만 나이 | self | 파라미터 [min,max] 포함 여부 |
| `housing_none` | 무주택 여부 | 무주택 여부 | self | 무주택 = true 요구 |
| `lease_type` | 임차 형태 | 전세/월세/… | self | 정책 허용 집합 포함 여부 |
| `income_self` | 본인 소득 | 월 소득(대략 가능) | self | 파라미터(중위 X%) 이하 여부 |
| `income_household` | 가구 소득 | (합산) | household_aggregate | 파라미터 이하 — 자가판정 불가 |
| `income_original` | 원가구 소득 | (부모 가구 합산) | household_aggregate | 동상 |
| `asset_self` | 본인 재산 | 재산액(대략) | self(느슨) | 파라미터 이하. 모름 허용 → unknown |
| `household_composition` | 가구 구성 | 세대 독립성·혼인 등 | self 또는 household_aggregate* | 세대 분리 시 self, 합산 필요 시 aggregate |
| `employment` | 재직 | 재직/구직 상태 | self | 허용 상태 포함 여부 |
| `education` | 학력 | 학력/재학 상태 | self | 허용 집합 포함 여부 |
| `benefit_overlap` | 중복수혜 | 기수혜 여부 | self | 배제 대상 미수혜 여부 |
| `separate_residence` | 별도 거주 인정 | (행정청 인정 여부) | admin_discretion | 시군구청장의 부모 별도거주 인정 여부. 자가입력 불가 → 항상 unknown(3.4) |

*`household_composition`의 값 유형은 정책 파라미터가 scope를 지정한다. 상세는 정책 규칙(4장·5장)에서 확정.
※ 임계값·중위 %는 여기 두지 않는다 — Phase 7에서 공식 출처로 검증해 정책 규칙에 넣는다(persona-matrix는 근사치).

### 2.4 ID 체계·공유

- 원자 ID는 소문자 스네이크. 영구·append-only, 폐기돼도 재사용 금지 (SPEC F-ID와 동일 원칙).
- 같은 원자를 여러 정책이 공유한다 (`age`·`housing_none`는 3개 MVP 정책 공통). 공유가 정규화 비용(RC2)을 선형에 가깝게 유지하는 근거다(PRD 8장①).
- 새 정책이 새 원자를 요구할 때만 카탈로그가 늘고 비용이 발생한다 (주거급여 4종째의 `parent_benefit`·`income_recognized` 파생이 그 예 — ADR-0002 D1).

## 3. 3치 논리 (Kleene)

원자는 {true, false, unknown}을 반환하고(2장), 정책 규칙은 이를 all_of(AND)·any_of(OR)·not으로 조합한다. 조합은 Kleene 강 3치 논리를 따른다. 최종 조합값 → verdict 사상은 1.3.

### 3.1 unknown의 의미와 출처

unknown은 "참일 수도 거짓일 수도 있어 아직 단정 불가"다. 세 출처가 있고, **논리는 출처를 구분하지 않는다**(모두 동일 unknown). 단 needs_review 안내 문구를 위해 출처 태그는 보존한다:

| 출처 | 예 | needs_review 안내 방향 |
|---|---|---|
| 입력 불확실 | 사용자가 '모름/대략' | "값을 확인하면 판정됩니다" |
| 행정 재량 | `admin_discretion` 원자 | "행정청 확인이 필요합니다" |
| 경계 | 임계 근처 대략값 | "정확한 값이면 갈립니다" |

### 3.2 진리표

**not**

| a | not a |
|---|---|
| true | false |
| false | true |
| unknown | unknown |

**all_of (AND, 이항)** — false가 흡수원소

| a \ b | true | false | unknown |
|---|---|---|---|
| **true** | true | false | unknown |
| **false** | false | false | false |
| **unknown** | unknown | false | unknown |

**any_of (OR, 이항)** — true가 흡수원소

| a \ b | true | false | unknown |
|---|---|---|---|
| **true** | true | true | true |
| **false** | true | false | unknown |
| **unknown** | true | unknown | unknown |

### 3.3 n항 일반화

- **all_of(…):** 하나라도 false면 false. 아니면 하나라도 unknown이면 unknown. 모두 true면 true.
- **any_of(…):** 하나라도 true면 true. 아니면 하나라도 unknown이면 unknown. 모두 false면 false.

흡수원소가 나오면 나머지를 평가하지 않아도 결과가 확정된다(예: all_of에서 false 하나 → 즉시 false). 구현은 이 단축 평가를 써도 되나 **의미의 원본은 위 정의**다.

### 3.4 값 유형과의 상호작용

- `self` 원자: 입력이 명확하면 true/false, '모름/대략'이면 unknown(입력 불확실 태그).
- `admin_discretion` 원자: 항상 unknown(행정 재량 태그). `any_of(자가판정경로, 재량경로)`에서 자가판정경로가 true면 **true로 해소**(unknown OR true = true) — 청년월세 면제군의 대체 경로가 성립하는 지점(2.2).
- `household_aggregate` 원자: 3치 평가에 **들어가지 않는다.** 규칙이 이 원자를 참조하면 정책은 평가 이전에 `out_of_scope`로 사전 분류된다(1.2·2.2). 즉 out_of_scope는 런타임 논리값이 아니라 규칙의 정적 속성이다.

### 3.5 경계 예시

- `all_of(age=true, income_self=unknown)` → unknown → needs_review (입력 확인 시 갈림)
- `all_of(age=false, income_self=unknown)` → false → 부적합 (나이에서 이미 탈락, 소득 몰라도 확정)
- `any_of(income_self=true, admin_discretion=unknown)` → true → 가능 (대체 경로 해소)
- `any_of(income_self=false, admin_discretion=unknown)` → unknown → needs_review (행정 재량 확인)

## 4. Rule DSL

정책의 자격 요건을 선언적으로 쓰는 JSON 트리. 기계 검증(형식)은 `contracts/rule-dsl.schema.json`, 의미(평가)는 3장.

### 4.1 구조

- 한 정책의 자격 요건 = **단일 루트 노드**의 트리.
- 노드는 4종: `all_of`, `any_of`, `not`, atom 참조.
- 트리는 3장 Kleene 논리로 평가되어 {true/false/unknown} → verdict(1.3).

### 4.2 노드 문법

**조합자**
```json
{ "all_of": [ <node>, ... ] }
{ "any_of": [ <node>, ... ] }
{ "not": <node> }
```

**원자 참조**
```json
{ "atom": "<atom_id>", "params": { ... }, "meta": { "source": "...", "year": 2026 } }
```
- `atom`: 2.3 카탈로그의 ID.
- `params`: 원자별 파라미터(임계값·허용집합). 파라미터가 없는 원자면 생략.
- `meta`: 출처·기준연도(SPEC F-004). 임계값이 매년 바뀌므로 파라미터 옆에 둔다. (용어 풀이는 원자 카탈로그 소유 — 정책마다 반복 안 함.)

### 4.3 원자별 params 규약

| 원자 | params | 예 |
|---|---|---|
| `age` | `{min?, max?}` (만 나이) | `{"min":19,"max":34}` |
| `housing_none` | 없음 (true 요구) | — |
| `lease_type` | `{allowed:[…]}` | `{"allowed":["jeonse"]}` |
| `income_self` | `{median_pct}` 또는 `{max_krw}` | `{"median_pct":60}` |
| `asset_self` | `{max_krw}` | `{"max_krw":345000000}` |
| `employment` | `{allowed:[…]}` | — |
| `education` | `{allowed:[…]}` | — |
| `benefit_overlap` | `{excluded:[policy_id]}` | — |
| `household_composition` | `{scope, married?}` | `{"scope":"self"}` |
| `income_household`·`income_original` | `household_aggregate` — 규칙에 등장하면 정책이 `out_of_scope`로 사전 분류(3.4). MVP 3정책엔 미등장 | — |

### 4.4 정책 레코드에서의 자리

Rule DSL 트리는 정책 레코드의 `rule` 필드다. 나머지(선발 방식·신청 시기 = F-007, 정책 id·이름·신청 링크 = F-006)는 레코드의 형제 필드이며 DSL 밖이다. **DSL은 자격 논리만 담당한다.** 정책 레코드 전체 형식은 `contracts/openapi.yaml` 응답에서 확정.

### 4.5 예시 A — 청년 주택드림 청약통장 (all_of)

```json
{
  "all_of": [
    { "atom": "age", "params": { "min": 19, "max": 34 }, "meta": { "source": "국토교통부", "year": 2026 } },
    { "atom": "housing_none", "meta": { "source": "국토교통부", "year": 2026 } },
    { "atom": "income_self", "params": { "max_krw": 50000000 }, "meta": { "source": "국토교통부", "year": 2026 } }
  ]
}
```
모두 `self` → 정상 3치 평가. 소득만 '모름'이면 → unknown → needs_review.

### 4.6 예시 B — 청년월세 면제군 (any_of + admin_discretion)

면제군 = 원가구 소득 평가를 면제받는 경로. 면제 경로를 `any_of`로 표현한다:

```json
{
  "all_of": [
    { "atom": "age", "params": { "min": 19, "max": 34 } },
    { "atom": "income_self", "params": { "median_pct": 60 } },
    { "any_of": [
      { "atom": "age", "params": { "min": 30 } },
      { "atom": "household_composition", "params": { "scope": "self", "married": true } },
      { "atom": "separate_residence" }
    ] }
  ]
}
```
`any_of` 안쪽이 면제 판정: 30세 이상=true거나 혼인=true면 → `unknown OR true = true`로 해소(가능). 자가 경로가 모두 false이고 재량만 unknown이면 → any_of unknown → needs_review("행정청 확인"). 3.4가 그대로 작동.

> 값·구조는 근사 예시다. 실제 파라미터와 면제 요건은 Phase 7에서 공식 출처로 검증해 확정한다(persona-matrix 근사치).

## 5. 대상 정책 (MVP)

> 각 정책의 **원자 구성**만 정리한다. 전체 rule 트리·임계값·허용집합은 Phase 7에서 공식 출처로 검증해 확정한다(persona-matrix는 근사치). 아래 원자 구성 자체도 Phase 7 검증 대상이다.

### 5.1 청년 주택드림 청약통장

- 사용 원자: `age`, `housing_none`, `income_self`
- 값 유형: 전부 `self` → 범위 안(판정 가능)
- 구조: `all_of` (§4.5 예시)
- 특이사항: 없음 — 가장 단순한 형태.

### 5.2 청년전용 버팀목 전세자금대출

- 사용 원자: `age`, `housing_none`, `lease_type`(전세), `income_self`, `asset_self`
- 값 유형: 전부 `self`(`asset_self`는 느슨) → MVP 페르소나(미혼) 기준 범위 안
- **열린 문제(Phase 7):** 기혼 신청자의 소득은 **부부합산**(→ `household_aggregate`)이다. 기혼 사용자를 scope 한정할지 `out_of_scope`로 보낼지 결정 필요. MVP 미혼 페르소나에선 `income_self`로 성립.

### 5.3 청년월세 특별지원 (면제군 한정)

- 사용 원자: `age`, `housing_none`, `lease_type`(월세), `income_self`, + 면제 게이트 `any_of(age≥30, household_composition[married], separate_residence)`
- 값 유형: `separate_residence` = `admin_discretion`(항상 unknown, 대체 경로로 해소, §3.4). 원 정책의 `income_original`(`household_aggregate`)은 **면제군 한정으로 규칙에서 배제**한다.
- 구조: `all_of` + 면제 `any_of` (§4.6 예시)
- **열린 문제(Phase 7):** 면제 게이트가 명확히 false인 사용자(30세 미만·미혼·재량 인정 없음)를 '부적합'이 아니라 `out_of_scope`로 보내야 FN을 막는다 — 이들은 배제한 `income_original` 경로로는 자격이 있을 수 있기 때문. D3의 "household_aggregate 참조 시 정적 out_of_scope"만으론 부족하고, **면제 게이트 false → out_of_scope** 규칙이 별도로 필요하다. 확정 시 ADR-0003에 결정 추가.

### 5.4 (later) 주거급여 청년 분리지급 — 4종째 후보

- 예상 신규 원자: `parent_benefit`(부모 수급 여부), `income_recognized`(소득인정액 파생 계산)
- 포함 여부는 신규 원자 비용 검토 후 결정(ADR-0002 D1). 값 유형·판정 가능성은 Phase 2~3 재검토 시 확정.
