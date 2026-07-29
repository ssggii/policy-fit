# 핸드오프 — 데일리 스크럼 Slack 자동화 (Hermes)

> 다른 세션이 같은 컨텍스트로 이어받기 위한 문서. GitHub 이슈 **#24**(Hermes/Slack 일일보고 자동화)의 작업 노트.
> 상위 큰그림·결정 배경은 프로젝트 메모리 `project-jira-github-slack-automation`에 있다. 이 문서는 그 중 "데일리 스크럼 자동화" 조각의 실행 상세.

## 0. 목표

스프린트 관리 · 일일 자동보고 · 티켓 기반 작업배정 중 **"매일 Slack 자동보고 = 데일리 스크럼 자동화"**에 해당한다.

- 매일 아침 Slack에 "어제 merge된 PR / 오늘 In Progress 티켓 / In Review 대기 / 막힌 것 / 스프린트 남은 용량"을 자동 push.
- 나아가 Slack에서 봇과 양방향 대화(오늘 티켓 묻기, 작업 지시)까지.
- **데이터 소스는 GitHub Projects**(트래커). Jira/POL은 폐기됨 — 혼동 금지.

## 1. 현재 상태 (2026-07-29 기준)

**완료**
- Hermes 설치 · Slack 게이트웨이 연동 (macOS launchd 서비스 `~/Library/LaunchAgents/ai.hermes.gateway.plist`).
- GitHub Projects 보드·자동화는 이미 라이브(별도, `project-automation.yml` merged). 보드/필드 ID는 §4 참조.

**미완 / 막힌 지점**
- **Anthropic 모델 연결 안 됨.** 구독 토큰(`claude setup-token` → `ANTHROPIC_TOKEN`) 경로를 시도했으나 불안정(로그인/로그아웃 시소, 자격증명 충돌 400)으로 실패.
- **결정: Claude API 키를 추가 결제해서 사용**(구독 아님). → §3-1이 다음 할 일.

## 2. 반드시 알아야 할 함정 (디버깅에서 얻은 교훈)

1. **launchd 서비스는 셸 환경변수(`.zshrc` 등)를 안 읽는다. `~/.hermes/.env`만 읽는다.** foreground(`hermes gateway run`)로 띄우면 셸 env를 물려받아 동작이 달라지므로 혼동 주의.
2. **`ANTHROPIC_API_KEY`와 `ANTHROPIC_TOKEN`이 동시에 세팅되면 SDK가 두 인증 헤더를 보내 API가 400으로 거부한다.** 정확히 하나만 남겨야 한다.
3. **빈 값 `ANTHROPIC_API_KEY=`(빈 문자열)도 "세팅된 것"으로 쳐서 400을 유발한다.** 안 쓸 변수는 **줄 자체를 삭제**한다.
4. 자격증명 종류 매칭: OAuth 구독 토큰(`sk-ant-oat...`)은 `ANTHROPIC_TOKEN`(Bearer), API 키(`sk-ant-api...`)는 `ANTHROPIC_API_KEY`. 교차하면 400.
5. `.env`를 바꾸면 **게이트웨이를 반드시 재시작**해야 반영된다 (`hermes gateway restart`). 떠 있는 프로세스는 시작 시점 값을 메모리에 들고 있다.
6. **디버깅은 `hermes gateway run`**(포그라운드)이 최고 — 터미널에 인증/연결 에러가 실시간으로 찍힌다. 되면 Ctrl+C 후 `hermes gateway start`로 백그라운드 전환.
7. **보안**: Hermes는 터미널 포함 full tools 접근 → Slack에서 봇에 말할 수 있는 사람 = 머신에서 명령 실행 가능. allowlist/DM pairing으로 **본인만** 허용.
8. **비용**: Hermes가 쓰는 API는 Claude Code 구독과 **별개 과금**. 이 볼륨(하루 1회 보고 + 가끔 대화)이면 월 소액.

## 3. 남은 작업 (순서대로)

### 3-1. API 키로 Anthropic 연결 마무리 (사용자 + 확인)
1. console.anthropic.com에서 API 키 발급(`sk-ant-api...`). ※ 구독(claude.ai)과 별개 계정·결제.
2. `~/.hermes/.env` 편집 → **정확히 이 상태로**:
   ```
   ANTHROPIC_API_KEY=sk-ant-api...발급키
   ```
   - `ANTHROPIC_TOKEN=` 줄은 **삭제**. (하나만 남긴다 — §2-2/2-3)
   - 확인: `grep ANTHROPIC ~/.hermes/.env` → API_KEY 한 줄만.
3. 셸에도 `ANTHROPIC_*` export가 없어야 함(launchd엔 무관하나 `run` 디버깅 시 충돌 방지): `grep -rn ANTHROPIC ~/.zshrc ~/.zprofile ~/.bashrc 2>/dev/null` → 있으면 제거.
4. `hermes gateway restart` → `hermes gateway status`가 loaded/running인지.
5. **검증**: Slack에서 봇에 DM → 응답 오면 배관 완성. 안 되면 `hermes gateway stop && hermes gateway run`으로 로그 확인.

### 3-2. 데일리 보고 스크립트 (에이전트가 작성)
- 위치(제안): `scripts/daily-report.sh` (youth-policy 리포).
- 내용: `gh` + GraphQL로 GitHub Projects(#1)에서 아래를 뽑아 사람이 읽을 요약 텍스트로 출력.
  - 어제(24h 내) merge된 PR
  - 현재 In Progress / In Review 아이템 (Status 필드)
  - 막힌 것(있으면; 라벨·코멘트 규약은 이때 정함)
  - 현재 스프린트(Sprint 필드) 남은 Todo 개수·Size 합
- **Hermes와 독립** — 배달 수단 무관하게 재사용. 만들면 채팅으로 "오늘 상황?"에도 바로 씀.
- 필요한 `gh` 스코프: 이미 `project`·`repo` 있음(PROJECTS 셋업 때 부여).

### 3-3. Hermes cron으로 매일 자동 push (에이전트 + 사용자)
- Hermes cron은 `~/.hermes/cron/*.yaml`에 정의.
- 매일 정해진 시각(예: 평일 오전 9시)에 3-2 스크립트를 실행 → 결과를 Slack 채널/DM으로 post하는 job 작성.
- ⚠️ launchd 서비스가 **켜져 있어야**(노트북이 깨어 있어야) cron이 돈다. 항상 켜두거나, 안 되면 always-on 호스트 고려.

### 3-4. 양방향 (에이전트 + 사용자)
- Slack에서 "오늘 내 티켓 뭐야?" → Hermes 뒤 에이전트가 `gh`로 Projects 조회 → 답변.
- "그럼 #7 착수해" → 브랜치 생성 등. loop.md 게이트와 어떻게 엮을지는 이때 설계.
- 3-3 안정화 후 진행.

## 4. 참조 (ID·사실)

- **GitHub Projects**: 프로젝트 #1, node id `PVT_kwHOB-H6j84BeqCp`, URL `https://github.com/users/ssggii/projects/1`, `ssggii/policy-fit`에 연결됨.
- **Status 필드** id `PVTSSF_lAHOB-H6j84BeqCpzhZCmf8` — 옵션: Todo `3f633c02` / In Progress `33cdd7b6` / In Review `588e371d` / Done `840ec0ec`.
- 보드 필드: Status · Size(S/M/L) · Sprint(단일선택) · Epic.
- **Sprint 1**: 수 2026-07-29 ~ 일 2026-08-02(5 캘린더일). 담긴 항목: #7(버팀목 정책, L) · #8(unknown_reasons 재설계, M) · #19(ci openapi 검증, S) · #21(질문 카피, S).
- 보드 상태 자동전환: `.github/workflows/project-automation.yml`(merged). 사용자 소유 Projects 접근용 classic PAT 시크릿 `PROJECTS_TOKEN`.
- **Hermes**: Nous Research 메시징 게이트웨이. CLI `hermes gateway {run,start,stop,restart,status,install,uninstall,setup,...}`. 설정 `~/.hermes/config.yaml`, 시크릿 `~/.hermes/.env`, cron `~/.hermes/cron/`. 문서 https://hermes-agent.nousresearch.com/docs
- 관련 메모리: `project-jira-github-slack-automation`(큰그림), `feedback-issue-first-workflow`, `feedback-no-direct-main-push`.
- 스프린트 운영 전반: `docs/sprint-workflow.md`.

## 5. 다음 세션 착수 지점

**바로 §3-1부터**(사용자가 API 키를 `.env`에 넣고 게이트웨이 재시작 → DM 검증). 검증되면 §3-2(데일리 보고 스크립트)를 에이전트가 작성 → §3-3(cron) 순으로.
