# ARCHITECTURE — 현재 구조 스냅샷

> 이 문서는 독립적으로 편집하지 않는다. accept된 ADR의 결과로만 갱신한다.
> 근거·트레이드오프는 담지 않는다 — 그것은 ADR의 역할.
> 상태: ADR-0004(기술 스택)·ADR-0006(백엔드 패키지 구조) accept 결과 반영.

## 1. 구조도

```
[단일 Git 저장소]

  web/  (Next.js App Router + TS)  ──POST /verdicts (HTTP)──▶  backend/  (Spring Boot, Java 21)
                                                                   판정 엔진
                                                                   (원자 평가기·Kleene 조합기·값 유형 사전분류)
  contracts/  (openapi · verdict · rule-dsl 스키마 = SSOT)
     ├─ TS 타입 생성   → web/
     └─ Java 모델 생성 → backend/
```

## 2. 구성요소와 책임 (각 한 줄)

- **web/** — Next.js(App Router)+TS. 분기 질문 입력·결과·근거·핸드오프 UI. 판정은 백엔드 호출로 수행.
- **backend/** — Spring Boot(Java 21). `POST /verdicts` 제공, 판정 엔진 실행. 입력 미저장·무로깅·처리 후 폐기.
- **판정 엔진** — rule DSL(JSON)을 파싱해 원자 평가 → Kleene 3치 조합 → verdict 사상. `household_aggregate` 원자 참조 정책은 사전 `out_of_scope` 분류. Java 직접 구현.
- **contracts/** — openapi·verdict.schema·rule-dsl.schema. 두 언어 타입 생성의 언어중립 SSOT.

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

## 5. 관련 ADR

| 영역 | ADR |
|---|---|
| 문서 체계 | adr/0001 |
| 판정 도메인 모델 | adr/0003 |
| 기술 스택 | adr/0004 |
| 백엔드 패키지 구조 | adr/0006 |
