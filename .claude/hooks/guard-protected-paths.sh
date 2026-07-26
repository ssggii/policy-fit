#!/usr/bin/env bash
# PreToolUse(Edit|Write): 보호 대상(계약·제품문서·CLAUDE.md) 편집 시 사람 확인(ask).
# loop.md 게이트 C + CLAUDE.md DO NOT의 기계적 강제. 매칭 없으면 무출력=allow.
set -euo pipefail
input=$(cat)
fp=$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty')
[ -z "$fp" ] && exit 0
case "$fp" in
  */contracts/*|*/adr/*|*/PRD.md|*/SPEC.md|*/DOMAIN.md|*/ARCHITECTURE.md|*/CLAUDE.md)
    jq -n --arg r "보호 대상(계약·제품문서·CLAUDE.md) 편집 — 사람 승인 필요 (loop.md 게이트 C / CLAUDE.md DO NOT)" \
      '{hookSpecificOutput:{hookEventName:"PreToolUse",permissionDecision:"ask",permissionDecisionReason:$r}}'
    ;;
esac
exit 0
