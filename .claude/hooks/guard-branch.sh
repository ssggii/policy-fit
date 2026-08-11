#!/usr/bin/env bash
# PreToolUse(Edit|Write): main 브랜치 직접 편집 하드 차단.
# CONTRIBUTING.md "main은 PR로만 병합, docs 전용도 예외 없이 브랜치+PR" 기계 강제(loop.md 게이트 B).
# ask는 에이전트 세션에서 무력할 수 있어(guard-protected-paths.sh 주석 참조) deny만 씀.
set -euo pipefail
branch=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")
if [ "$branch" = "main" ]; then
  jq -n --arg r "main에서 직접 편집 금지 — 먼저 git checkout -b type/<이슈#>-slug 후 git push -u origin <branch> (CONTRIBUTING.md, loop.md 게이트 B)" \
    '{hookSpecificOutput:{hookEventName:"PreToolUse",permissionDecision:"deny",permissionDecisionReason:$r}}'
  exit 0
fi
exit 0
