#!/usr/bin/env bash
# PreToolUse(Edit|Write): 보호 대상 편집 가드.
# - contracts/ · CLAUDE.md → deny (하드 차단; 에이전트 직접 편집 금지. freeze / CLAUDE.md DO NOT)
# - 제품문서(PRD·SPEC·DOMAIN·ARCHITECTURE·adr) → ask (소프트 백스톱; 승인 기반 diff 편집 대상)
# 매칭 없으면 무출력 = allow.
# 주: 에이전트 세션에선 "ask"가 프롬프트로 안 이어질 수 있음(무력) — "deny"만 전 모드에서 차단.
set -euo pipefail
input=$(cat)
fp=$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty')
[ -z "$fp" ] && exit 0
case "$fp" in
  */contracts/*|*/CLAUDE.md)
    jq -n --arg r "계약(contracts/)·CLAUDE.md는 에이전트 직접 편집 금지 — 사람이 편집하거나 훅을 잠시 비활성화하라 (freeze / CLAUDE.md DO NOT)" \
      '{hookSpecificOutput:{hookEventName:"PreToolUse",permissionDecision:"deny",permissionDecisionReason:$r}}'
    ;;
  */adr/*|*/PRD.md|*/SPEC.md|*/DOMAIN.md|*/ARCHITECTURE.md)
    jq -n --arg r "제품문서 편집 — 사람 승인 필요 (loop.md 게이트 C / diff 제안 원칙)" \
      '{hookSpecificOutput:{hookEventName:"PreToolUse",permissionDecision:"ask",permissionDecisionReason:$r}}'
    ;;
esac
exit 0
