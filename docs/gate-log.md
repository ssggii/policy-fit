# 게이트 차단 로그

> `loop.md` §8.5 관측. 게이트가 실제로 작동했음을 보여주는 기록이자, `loop.md` §9 회고의 3회 누적/반복 차단 판단 근거.
> 차단이 발생할 때마다 커밋하지 않는다 — 작업 사이클(세션) 동안 누적했다가, 그 사이클의 최종 커밋 직전에 한 번에 append한다.

| 날짜 | 게이트 | 사유 | 작업 | 참고 |
|---|---|---|---|---|
| 2026-07-27 | 3 | `IncomeSelfAtomEvaluator`가 `approx=true`를 DOMAIN §3.4를 위반해 `boundary`로 태깅(실제는 `input_uncertain`) — 코드 주석도 "DOMAIN 확정 규칙"이라 잘못 표기 | 백엔드 판정 엔진, 청년 주택드림 청약통장 (F-003) | consistency-reviewer가 발견, 같은 세션에서 즉시 수정 (커밋 `7b9909d`) |
| 2026-08-03 | C | freeze 훅이 `contracts/openapi.yaml`(married 필드·enum 확장)의 에이전트 직접 편집을 차단 — 계약 변경은 사람 승인·편집 필요 | 청년월세 범위 게이트 (F-003/F-007, #10) | 사람이 검토 포인트 승인 후 직접 편집해 해소, 에이전트가 diff 정합성 검증 |
| 2026-08-08 | 3-1 | code-reviewer가 #42 초기 구현에서 PolicySelect·정책 카탈로그가 CheckFlow에 미연결(죽은 코드·AC1 미충족) + CollectedAnswers 타입 안전성 후퇴를 지적 | 웹 정책 선택·입력 질문 확장 (F-001, #42) | 스코프 A(wiring 포함)로 재확인 → CheckFlow wiring + 타입 강화 + 테스트 보강, 재리뷰에서 해소 |
| 2026-08-08 | 3-1 | code-reviewer가 #22 디자인 반영에서 (a) unlayered `:focus-visible{border-radius}`가 포커스 시 라운드 요소 붕괴(Playwright 실측) (b) `/check` h1 소실(a11y)을 지적 | 웹 디자인 반영 (F-001, #22) | SSOT를 그대로 옮긴 focus 규칙의 Tailwind cascade-layer 부작용 + h1 회귀 → radius 제거·화면별 h1 승격·F-005 대비 상향으로 수정, 재리뷰 통과 |
