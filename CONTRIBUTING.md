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
  - 본문은 **산문 단락이 아니라 짧은 불릿 3~5개**로 쓴다(제목 → 빈 줄 → 불릿 → 필요 시 푸터).

예시:
- `feat(backend): verdict 3치 조합기 구현` + 푸터 `Refs: F-003`
- `docs(spec): SPEC 범위표 작성`
- `fix(web): 세대 독립성 질문 분기 오류`

## 브랜치

- feature 브랜치에서 작업: `type/<이슈#>-<F-ID>-슬러그` (예: `feat/7-F002-beotimmok`)
  - **이슈 번호는 필수** — GitHub Projects 자동화(`project-automation.yml`)가 브랜치명 앞의 이슈 번호로 보드 아이템을 In Progress로 전환한다.
  - F-ID가 없는 작업(chore·docs·인프라)은 F-ID를 생략: `chore/23-projects-automation`.
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
- F-ID 작업(등급 B/C)은 **착수 전 이슈 초안을 먼저 제시**하고 등록을 확인받은 뒤 구현에 들어간다(트리비얼 등급 A 제외). Jira 연동 시 자동화로 대체.
- 완료 시 **실제로 완료한 완료조건 체크박스만 직접 `[x]`** 표시한 뒤 이슈를 닫는다 — 보드 자동화는 Status만 바꾸고 본문 체크박스는 건드리지 않는다. 부분 완료는 완료한 것만 체크한다.

## DoD (Definition of Done)

작업이 "완료"로 인정되는 보편 기준(모든 작업 공통):

1. 해당 F-ID의 **SPEC 수용 기준을 충족**한다.
2. `loop.md` **게이트를 통과**한다 (테스트 green · 리뷰 통과 · 계약/의존성 승인).
3. 문서 영향(SPEC·DOMAIN·ARCHITECTURE·ADR)이 반영되거나 diff로 제안됐다.

수용 기준(기능별 "무엇이 참이어야 하나")과 DoD(모든 작업 공통 "완료 바")는 함께 성립해야 한다.

## Jira 연동 (예약 — 원격·Jira 프로젝트 생성 후 활성)

Jira-GitHub 연동은 **Jira 이슈 키**(예: `PROJ-123`)를 브랜치·커밋·PR에서 스캔해 이슈에 링크한다. 활성 시 아래를 적용한다(프로젝트 키 `<JIRA-KEY>`는 확정 후 치환):

- **브랜치**: `type/<JIRA-KEY>-슬러그` (예: `feat/PROJ-123-verdict`)
- **커밋**: 제목 또는 본문에 `<JIRA-KEY>` + 기존 `Refs: F-ID` 푸터 **유지** — 커넥터 링크와 git 영구 spec 추적을 둘 다 확보(옵션 A).
- **PR 제목**: `<JIRA-KEY>` 포함. CI의 PR-title 린트는 Jira 키를 허용하도록 구성한다.
- **원칙 유지**: SPEC·`contracts/`는 Jira를 모른다. Jira 이슈가 F-ID를 참조하는 **단방향**. F-ID는 git에 영구(SSOT), Jira 키는 소모성 작업 추적용.
