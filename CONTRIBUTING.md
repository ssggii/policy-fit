# CONTRIBUTING — 협업 컨벤션

> youth-policy의 커밋·브랜치·PR·이슈 규칙. 작업 루프는 `loop.md`, 만들 것은 `SPEC.md`, 결정 근거는 `adr/`.
> 1인 개발 + AI 에이전트 루프 기준. 원격(GitHub) 연결 후 PR·브랜치 보호가 활성된다.

## 커밋 — Conventional Commits

형식: `type(scope): summary`

- **type**: `feat` · `fix` · `docs` · `chore` · `refactor` · `test` · `build` · `ci` · `perf`
- **scope**(선택, 영역): `backend` · `web` · `contracts` · `domain` · `spec` · `adr` · `loop` · `ci`
- **summary**: 간결·명령형에 준함
- **푸터(선택)**: 관련 SPEC 기능 ID를 `Refs: F-003`으로 표기할 수 있다(어느 SPEC 기능인지 추적용). 필수는 아니며, 이슈·PR 연결은 GitHub 이슈#·PR `Closes #N`이 담당한다.
- **Breaking change**: 본문에 `BREAKING CHANGE: ...`
- **본문(선택)**: 변경 이유가 자명하지 않으면 "왜/무엇"을 적는다. `Refs: F-ID` 푸터는 선택, 트리비얼(등급 A)은 제목만 허용.
  - 본문은 **산문 단락이 아니라 짧은 불릿 3~5개**로 쓴다(제목 → 빈 줄 → 불릿 → 필요 시 푸터).

예시:
- `feat(backend): verdict 3치 조합기 구현` + 푸터 `Refs: F-003`
- `docs(spec): SPEC 범위표 작성`
- `fix(web): 세대 독립성 질문 분기 오류`

## 브랜치

- feature 브랜치에서 작업: `type/<이슈#>-슬러그` (예: `feat/42-policy-select`). SPEC F-ID를 넣어도 되나(`feat/7-F002-beotimmok`) 선택이다.
  - **이슈 번호는 필수** — GitHub Projects 자동화(`project-automation.yml`)가 브랜치명 앞의 이슈 번호로 보드 아이템을 In Progress로 전환한다.
- 브랜치 생성 직후 **구현 전에 바로 `git push -u`** 한다 — `create` 웹훅으로 보드가 In Progress로 전환되므로 착수 시점과 맞춘다.
- `main`은 직접 커밋하지 않고 **PR로만 병합**한다(브랜치 보호 활성). 관리자 권한이라 `git push origin main`이 보호를 우회하므로 **docs 전용이라도 예외 없이** 브랜치+PR을 거치고, push 전 현재 브랜치가 `main`인지 확인한다.

### PR merge 후 로컬 동기화 (squash merge)

squash merge는 origin/main에 새 커밋을 만들어 로컬과 갈라진다. merge 후:

```
git fetch origin && git checkout main
git reset --hard origin/main
git remote prune origin
```

## PR

- `.github/PULL_REQUEST_TEMPLATE.md`를 따른다. 생성 직전 **매번 `cat`으로 재확인**하고 섹션·순서 그대로 채운다 — 자체 구조를 만들지 않는다.
- PR 체크리스트로 `loop.md` 게이트를 확인한다: 테스트 green(게이트 2) · 정합성·리뷰(3·3-1) · 계약 변경 승인(C) · 수용 기준 충족. 실제로 돌린 게이트만 체크하고, 생략한 게이트는 사유를 적는다.
- **연관 이슈**: 완료 조건이 **전부 `[x]`면 `Closes #N`**, 하나라도 `[ ]`면 **`Refs #N`**(병합 시 자동 닫힘 방지).
- **본문 문체**: 설명형 산문 섹션(개요·변경 내용·리뷰 포인트·배경)은 **"~습니다"체**로 쓴다(목록·체크박스·표는 제외). "변경 내용"은 긴 줄글 대신 **제목 붙인 불릿**으로 구성한다. 이슈 본문도 동일.

## 이슈

- `.github/ISSUE_TEMPLATE/`의 **feature / bug** 템플릿 사용.
- feature 이슈는 **SPEC F-ID에 연결**한다.
- F-ID 작업(등급 B/C)은 **착수 전 이슈 초안을 먼저 제시**하고 등록을 확인받은 뒤 구현에 들어간다(트리비얼 등급 A 제외).
- 완료 시 **실제로 완료한 완료조건 체크박스만 직접 `[x]`** 표시한 뒤 이슈를 닫는다 — 보드 자동화는 Status만 바꾸고 본문 체크박스는 건드리지 않는다. 부분 완료는 완료한 것만 체크한다.

## DoD (Definition of Done)

작업이 "완료"로 인정되는 보편 기준(모든 작업 공통):

1. 해당 F-ID의 **SPEC 수용 기준을 충족**한다.
2. `loop.md` **게이트를 통과**한다 (테스트 green · 리뷰 통과 · 계약/의존성 승인).
3. 문서 영향(SPEC·DOMAIN·ARCHITECTURE·ADR)이 반영되거나 diff로 제안됐다.

수용 기준(기능별 "무엇이 참이어야 하나")과 DoD(모든 작업 공통 "완료 바")는 함께 성립해야 한다.

## 스프린트·티켓

스프린트 관리·티켓·일일 보고는 **GitHub Projects(v2)**로 운영한다(Jira는 쓰지 않는다). 보드·자동화·필드 규약은 `docs/sprint-workflow.md`를 따른다.
