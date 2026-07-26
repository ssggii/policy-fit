# CONTRIBUTING — 협업 컨벤션

> youth-policy의 커밋·브랜치·PR·이슈 규칙. 작업 루프는 `loop.md`, 만들 것은 `SPEC.md`, 결정 근거는 `adr/`.
> 1인 개발 + AI 에이전트 루프 기준. 원격(GitHub) 연결 후 PR·브랜치 보호가 활성된다.

## 커밋 — Conventional Commits

형식: `type(scope): summary`

- **type**: `feat` · `fix` · `docs` · `chore` · `refactor` · `test` · `build` · `ci` · `perf`
- **scope**(선택, 영역): `backend` · `web` · `contracts` · `domain` · `spec` · `adr` · `loop` · `ci`
- **summary**: 간결·명령형에 준함
- **푸터**: 관련 SPEC 기능 ID를 `Refs: F-003`으로 표기 (추적성 — 후속 Jira가 F-ID를 참조하는 단방향 링크)
- **Breaking change**: 본문에 `BREAKING CHANGE: ...`
- **본문(선택)**: 변경 이유가 자명하지 않으면 "왜/무엇"을 적는다. 기능·버그 커밋은 `Refs: F-ID` 푸터 필수, 트리비얼(등급 A)은 제목만 허용.

예시:
- `feat(backend): verdict 3치 조합기 구현` + 푸터 `Refs: F-003`
- `docs(spec): SPEC 범위표 작성`
- `fix(web): 세대 독립성 질문 분기 오류`

## 브랜치

- feature 브랜치에서 작업: `type/F-ID-슬러그` (예: `feat/F-003-verdict`, `fix/F-001-skip-logic`)
- F-ID가 없는 작업은 `chore/...`·`docs/...`.
- `main`은 직접 커밋하지 않고 **PR로만 병합**한다(원격 연결 후 브랜치 보호 활성).

## PR

- `.github/PULL_REQUEST_TEMPLATE.md`를 따른다.
- PR 체크리스트로 `loop.md` 게이트를 확인한다: 테스트 green(게이트 2) · 정합성·리뷰(3·3-1) · 계약 변경 승인(C) · 수용 기준 충족.

## 이슈

- `.github/ISSUE_TEMPLATE/`의 **feature / bug** 템플릿 사용.
- feature 이슈는 **SPEC F-ID에 연결**한다.

## DoD (Definition of Done)

작업이 "완료"로 인정되는 보편 기준(모든 작업 공통):

1. 해당 F-ID의 **SPEC 수용 기준을 충족**한다.
2. `loop.md` **게이트를 통과**한다 (테스트 green · 리뷰 통과 · 계약/의존성 승인).
3. 문서 영향(SPEC·DOMAIN·ARCHITECTURE·ADR)이 반영되거나 diff로 제안됐다.

수용 기준(기능별 "무엇이 참이어야 하나")과 DoD(모든 작업 공통 "완료 바")는 함께 성립해야 한다.
