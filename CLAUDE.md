# 폴핏 (policy-fit)

자격 요건을 판정하여 지금 신청할 수 있는 청년 정책을 빠짐없이 보여주는 서비스.
"신청 가능"은 신청 자격 충족을 의미하며, 선발·수혜(추첨, 선착순 등)를 보장하지 않는다.
1인 개발 + AI 에이전트 루프로 운영한다. 이 파일은 에이전트가 매 세션 가장 먼저 읽는 진입점이다.

## 문서 맵

### 제품 문서 — 사람이 결정한다. 에이전트는 읽기만 하고, 수정이 필요하면 제안한다.

| 문서 | 역할 |
|---|---|
| `PRD.md` | 왜 만드는가. 타겟, 성공 기준, out-of-scope와 사유. 최상위 문서. |
| `SPEC.md` | 무엇을 만드는가. 기능 목록(영구 ID) + 수용 기준 + MVP in/out/later 범위표. |
| `DOMAIN.md` | 도메인 의미론. 정책 요건 원자, 3치 논리, rule DSL 문법. |
| `ARCHITECTURE.md` | 현재 아키텍처 스냅샷. 근거는 담지 않는다(근거는 ADR). |
| `adr/` | 구조적 결정 기록. Context / Decision / 기각된 대안. |

### 계약 파일 — 사람이 승인하고, CI와 hook이 검증한다.

| 파일 | 역할 |
|---|---|
| `contracts/openapi.yaml` | API 요청/응답 계약. 코드 생성·검증에 직접 사용. |
| `contracts/verdict.schema.json` | 판정 결과 4-state enum 스키마. |
| `contracts/rule-dsl.schema.json` | rule DSL 문법의 기계 검증용 스키마. |

### 운영 문서 — 에이전트가 매 세션 참조한다. /retro를 통해 갱신한다.

| 문서 | 역할 |
|---|---|
| `CLAUDE.md` | 이 파일. 문서 맵, 라우팅, 절대 규칙. 200줄 이하 유지. |
| `loop.md` | 작업 루프 정의. 작업 단위, 게이트, 단계 규칙. |
| `.claude/agents/` `.claude/hooks/` `.claude/skills/` | 하네스 설정. 각 파일 헤더에 강제/참조하는 문서를 명시. |

## 라우팅 — 이런 일이 생기면 이 문서로 간다

- 판정 규칙·DSL·3치 논리 변경 → `DOMAIN.md`
- 기능 추가·변경 요청 → `SPEC.md`의 범위표를 먼저 확인. out/later면 구현하지 않고 보고
- 아키텍처·스택·구조 변경 → `adr/`에 새 ADR 작성 제안이 먼저. accept 전에는 착수 금지
- API 요청/응답 형식 변경 → `contracts/` 변경 제안 + 사람 승인 필수
- "왜 이렇게 되어 있지?" → `PRD.md`와 `adr/`에서 근거 확인. 없으면 사람에게 질문
- 작업 순서·게이트 판단 → `loop.md`

## 문서 규칙

- 충돌 시 우선순위: `PRD.md` > `SPEC.md` = `DOMAIN.md` > `ARCHITECTURE.md` > 운영 문서
- 제품 문서는 append-only. 기능 ID는 한번 부여되면 영구적이며 재사용하지 않는다
- ADR은 수정·삭제하지 않는다. 결정 변경은 새 ADR 작성 + 옛 ADR에 superseded 표기
- `ARCHITECTURE.md`는 독립적으로 편집하지 않는다. accept된 ADR의 결과로만 갱신한다
- 서브도메인이 2개가 되면 `DOMAIN.md`를 `domain/` 디렉토리로 분리한다

## 기술 스택

- ADR-0004 확정. 상세·근거는 adr/0004.
  - 백엔드: Java 21 + Spring Boot 4.x (판정 엔진 직접 구현, `POST /verdicts`)
  - 프론트: Next.js (App Router) + TypeScript
  - 계약: `contracts/`(openapi·JSON Schema) → Java 모델 + TS 타입 생성 (SSOT)
  - 테스트: 백엔드 JUnit 5 / 프론트 Vitest + Playwright
  - 빌드: 백엔드 Gradle · 프론트 pnpm
  - 구조: 단일 저장소(`backend/`·`web/`·`contracts/`), 두 서비스 독립 배포
  - 배포: 프론트 Vercel · 백엔드 JVM 호스트(미정, 5b에서 확정)

## 자주 쓰는 명령어

백엔드 (`backend/`):
  - 빌드·테스트: `./gradlew build`
  - 로컬 실행: `./gradlew bootRun`
  
프론트 (`web/`):
  - 개발 서버: `pnpm dev`
  - 빌드: `pnpm build`
  - 린트: `pnpm lint`


## DO NOT - 절대 금지

- `contracts/` 파일을 사람 승인 없이 수정하지 마라
- 제품 문서(`PRD.md`, `SPEC.md`, `DOMAIN.md`, `ARCHITECTURE.md`, `adr/`)를 직접 수정하지 마라. 변경이 필요하면 diff를 제안하라
- accept된 ADR 없이 아키텍처·스택을 변경하지 마라
- `SPEC.md` 범위표에서 out 또는 later인 기능을 구현하지 마라
- 외부 라이브러리 추가 전 반드시 승인을 요청하라
- 이 파일(CLAUDE.md)을 직접 수정하지 마라. /retro를 통해 제안하고 사람이 승인한다

<!--
운영 메모 (사람용):
- 이 파일은 200줄 이하 유지. 넘치면: 스타일 규칙 → skills/, 강제 규칙 → hooks/, 경로별 규칙 → .claude/rules/ (globs)
- 에이전트가 같은 실수를 두 번 하면 DO NOT에 즉시 한 줄 추가
- 모델 세대가 바뀌면 불필요해진 규칙 삭제
- 일시적 지시(특정 파일만 작업 등)는 CLAUDE.local.md에 (.gitignore 처리)
-->
