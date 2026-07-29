#!/usr/bin/env bash
# 데일리 진행 보고 — GitHub Projects(#1)에서 어제 merge된 PR / In Progress / In Review /
# 막힌 것(blocked 라벨) / 현재 스프린트 남은 Todo를 사람이 읽을 텍스트로 출력한다.
# Hermes와 독립적으로 동작한다 — `bash scripts/daily-report.sh`로 직접 실행 가능
set -euo pipefail
trap 'echo "❌ daily-report 실패 (exit $?) — 위 gh 에러 참조" >&2' ERR

REPO="ssggii/policy-fit"
PROJECT_ID="PVT_kwHOB-H6j84BeqCp"

# macOS(BSD date)와 GNU date 양쪽 지원
SINCE_DATE=$(date -u -v-1d +%Y-%m-%d 2>/dev/null || date -u -d '1 day ago' +%Y-%m-%d)
TODAY_DATE=$(date -u +%Y-%m-%d)

# 상한(오늘)을 명시하지 않으면 "어제 merge"가 그 이후 실행마다 계속 잡힌다 — 정확히 어제 하루로 고정
MERGED_JSON=$(gh pr list --repo "$REPO" --state merged \
  --search "merged:${SINCE_DATE}..${SINCE_DATE}" \
  --json number,title,url,mergedAt --limit 50)

ITEMS_JSON=$(gh api graphql -f query='
query($project: ID!) {
  node(id: $project) {
    ... on ProjectV2 {
      items(first: 100) {
        pageInfo { hasNextPage }
        nodes {
          content {
            ... on Issue { number title url labels(first: 10) { nodes { name } } }
            ... on PullRequest { number title url labels(first: 10) { nodes { name } } }
          }
          fieldValues(first: 20) {
            nodes {
              ... on ProjectV2ItemFieldSingleSelectValue {
                name
                field { ... on ProjectV2FieldCommon { name } }
              }
            }
          }
        }
      }
    }
  }
}' -f project="$PROJECT_ID")

MERGED_JSON="$MERGED_JSON" ITEMS_JSON="$ITEMS_JSON" SINCE_DATE="$SINCE_DATE" TODAY_DATE="$TODAY_DATE" python3 <<'PYEOF'
import json
import os
import re
import sys

merged = json.loads(os.environ["MERGED_JSON"])
project_items = json.loads(os.environ["ITEMS_JSON"])["data"]["node"]["items"]
items = project_items["nodes"]
if project_items["pageInfo"]["hasNextPage"]:
    print("⚠️ 프로젝트 아이템이 100건을 넘어 일부가 집계에서 누락됐습니다 (페이지네이션 미구현)", file=sys.stderr)


def field_values(item):
    return {
        fv["field"]["name"]: fv["name"]
        for fv in item["fieldValues"]["nodes"]
        if fv
    }


in_progress, in_review, blocked, todo_items = [], [], [], []
todo_by_size = {"S": 0, "M": 0, "L": 0}
sprint_numbers = {}  # "Sprint N" -> N, 등장한 모든 값(완료 여부 무관)에서 최댓값을 현재 스프린트로 본다

for item in items:
    content = item.get("content") or {}
    if not content:
        continue
    fv = field_values(item)
    status = fv.get("Status")
    sprint = fv.get("Sprint")
    labels = {n["name"] for n in content.get("labels", {}).get("nodes", [])}

    if sprint:
        # 보드에 "현재 스프린트"를 나타내는 필드가 따로 없다 — Sprint 필드가
        # "Sprint N" 규약을 따른다는 전제(docs/sprint-workflow.md) 하에 N이 가장
        # 큰 값을 현재 스프린트로 추정한다. 이 규약이 깨지면(예: 이름 변경) 오탐한다.
        m = re.search(r"(\d+)", sprint)
        if m:
            sprint_numbers[sprint] = int(m.group(1))
    if status == "In Progress":
        in_progress.append(content)
    if status == "In Review":
        in_review.append(content)
    if status != "Done" and "blocked" in labels:
        blocked.append(content)

current_sprint = max(sprint_numbers, key=sprint_numbers.get) if sprint_numbers else None

if current_sprint:
    for item in items:
        content = item.get("content") or {}
        if not content:
            continue
        fv = field_values(item)
        if fv.get("Sprint") == current_sprint and fv.get("Status") == "Todo":
            size = fv.get("Size", "?")
            todo_by_size[size] = todo_by_size.get(size, 0) + 1
            todo_items.append((content, size))


def line(c):
    return f"• [#{c['number']}]({c['url']}) {c['title']}"


def section(emoji, title, contents):
    print(f"{emoji} {title} ({len(contents)})")
    if contents:
        for c in contents:
            print(line(c))
    else:
        print("없음")
    print()
    print()


since_short = os.environ["SINCE_DATE"][5:]  # MM-DD
today = os.environ["TODAY_DATE"]

if current_sprint:
    total_todo = sum(todo_by_size.values())
    breakdown = " · ".join(f"{k}{v}" for k, v in todo_by_size.items() if v) or "없음"
    sprint_line = f"{current_sprint} 남은 Todo {total_todo}건"
else:
    sprint_line = "진행 중인 스프린트 없음"

# 순서: 어제 merge → In Progress → In Review → Blocked → Sprint 남은 ToDo
# (제목만 볼드 처리, 나머지는 굵게/기울임/백틱 없이 순수 텍스트로 출력)
print(f"📝 **데일리 진행 보고 — {today}**")
print()
print(
    f"> 요약 : 어제 merge {len(merged)}건 · In Progress {len(in_progress)} · "
    f"In Review {len(in_review)} · Blocked {len(blocked)} · {sprint_line}"
)
print()
print("───")
print()

print(f"✅ 어제 merge ({len(merged)})")
if merged:
    for pr in merged:
        print(f"• [#{pr['number']}]({pr['url']}) {pr['title']}")
else:
    print("없음")
print()
print()

section("💡", "In Progress", in_progress)
section("👀", "In Review", in_review)
section("🚨", "Blocked", blocked)

if current_sprint:
    print(f"📦 {current_sprint} 남은 ToDo — {total_todo}건")
    if todo_items:
        for content, size in todo_items:
            print(f"• [#{content['number']}]({content['url']}) {content['title']} ({size})")
    else:
        print("없음")
else:
    print("📦 진행 중인 스프린트 없음")
print()
PYEOF
