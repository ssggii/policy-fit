#!/usr/bin/env bash
# PreToolUse(Bash): 외부 패키지 추가 명령 감지 시 사람 확인(ask).
# loop.md 게이트 D + CLAUDE.md DO NOT의 기계적 강제.
# - `add` 서브커맨드(pnpm/npm/yarn add)는 플래그 위치와 무관하게 항상 감지.
# - `npm install|i`는 패키지 인자가 바로 뒤따를 때만(복원용 bare install은 통과).
# - 한계: `npm install --flag pkg`(선행 플래그+패키지)는 놓칠 수 있음 → PR 리뷰가 보조.
set -euo pipefail
input=$(cat)
cmd=$(printf '%s' "$input" | jq -r '.tool_input.command // empty')
[ -z "$cmd" ] && exit 0
padded=" $cmd "

ask() {
  jq -n --arg r "외부 라이브러리 추가 감지 — 사람 승인 필요 (loop.md 게이트 D / CLAUDE.md DO NOT)" \
    '{hookSpecificOutput:{hookEventName:"PreToolUse",permissionDecision:"ask",permissionDecisionReason:$r}}'
}

if printf '%s' "$padded" | grep -Eq ' (pnpm|npm|yarn) '; then
  if printf '%s' "$padded" | grep -Eq ' add '; then
    ask
  elif printf '%s' "$padded" | grep -Eq ' (install|i) +[^-[:space:]]'; then
    ask
  fi
fi
exit 0
