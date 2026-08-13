# ARCHITECTURE — 현재 구조 스냅샷

> 이 문서는 독립적으로 편집하지 않는다. accept된 ADR의 결과로만 갱신한다.
> 근거·트레이드오프는 담지 않는다 — 그것은 ADR의 역할.
> 상태: ADR-0004(기술 스택)·ADR-0006(백엔드 패키지 구조)·ADR-0008(정책 데이터 신뢰성) accept 결과 반영.

## 1. 구조도

```
[단일 Git 저장소]

  web/  (Next.js App Router + TS)  ──POST /verdicts (HTTP)──▶  backend/  (Spring Boot, Java 21)
                                                                   판정 엔진
                                                                   (원자 평가기·Kleene 조합기·값 유형 사전분류)
  contracts/  (openapi · verdict · rule-dsl 스키마 = SSOT)
     ├─ TS 타입 생성   → web/
     └─ Java 모델 생성 → backend/

  policies/*.json  (rule + provenance + covered_requirements)   ← 판정 엔진이 로드
     ├─ CI provenance 게이트 (미검증·만료 검사)
     └─ 주기 감시 잡 (GitHub Actions) ──이슈 생성──▶ 사람 재검증
```

## 2. 구성요소와 책임 (각 한 줄)

- **web/** — Next.js(App Router)+TS. 분기 질문 입력·결과·근거·핸드오프 UI. 판정은 백엔드 호출로 수행.
- **backend/** — Spring Boot(Java 21). `POST /verdicts` 제공, 판정 엔진 실행. 입력 미저장·무로깅·처리 후 폐기.
- **판정 엔진** — rule DSL(JSON)을 파싱해 원자 평가 → Kleene 3치 조합 → verdict 사상. `household_aggregate` 원자 참조 정책은 사전 `out_of_scope` 분류. Java 직접 구현.
- **contracts/** — openapi·verdict.schema·rule-dsl.schema. 두 언어 타입 생성의 언어중립 SSOT.
- **정책 레코드(`backend/.../policies/*.json`)** — `rule`(DSL) + `provenance`(출처·tier·검증일·재검증기한) + `covered_requirements`(원문 요건↔원자 매핑·미커버 사유). git이 승인 게이트이자 감사 로그다(ADR-0008 D1).
- **감시 잡(GitHub Actions)** — `quote` 소실·재검증 만료·출처 소실·API 대조 불일치를 결정론으로 감지해 **이슈를 생성한다.** 값을 자동 변경하지 않는다(D4·D8).

### 2.1 백엔드 내부 구조 (도메인 중심 배치 — ADR-0006)

기술 계층(Controller→Service→Repository)이 아니라 도메인 개념으로 패키지를 나눈다.

- `domain/`(atom·rule·evaluate·kleene·scope·verdict)·`policy/` — 순수 POJO, `org.springframework` 의존 없음. 판정 엔진의 계산 로직.
- `api/` — Spring inbound 어댑터(Controller·ExceptionHandler·DTO·요청 매퍼). 얇게 유지, 판정 로직 없음.
- `config/` — Spring 부트스트랩.
- 의존 방향: `api` → `domain`/`policy` 단방향. 도메인은 web·HTTP·프레임워크를 모른다. 저장 없음 → Repository 계층 없음.

## 3. 의존 방향 규칙

- `web` → (HTTP `POST /verdicts` 계약) → `backend`. 단방향.
- `web`·`backend` → `contracts` (읽기, 타입 생성). `contracts`는 어느 구현에도 의존하지 않는다.
- `contracts`는 구현 중 고정(SSOT). 변경은 사람 승인(CLAUDE.md DO NOT).

## 4. 빌드·배포

- 빌드: `backend` = Gradle, `web` = pnpm (독립).
- 배포: `web` = Vercel, `backend` = JVM 호스트(미정 — ADR-0004 D6, 5b에서 확정). 두 서비스 독립 배포.
- CI: `contracts` 검증 + **provenance 게이트**(미검증 `tier:3`·`recheck_after` 만료 검사. 1단계는 경고, 요건 커버리지 해소 후 빌드 실패로 승격 — ADR-0008 D6).
- 감시: GitHub Actions 스케줄(만료 검사 일 1회 · `quote`/출처/API 대조 주 1회 + heartbeat). 배포 확정 전까지 백엔드에 두지 않는다(D8).

## 5. 관련 ADR

| 영역 | ADR |
|---|---|
| 문서 체계 | adr/0001 |
| 판정 도메인 모델 | adr/0003 |
| 기술 스택 | adr/0004 |
| 백엔드 패키지 구조 | adr/0006 |
| 정책 데이터 신뢰성 | adr/0008 |
