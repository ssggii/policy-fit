"use client";

import type { PolicyId } from "@/lib/types/verdict";
import { POLICIES } from "./policies";

/**
 * 어떤 정책을 판정할지 고르는 화면. 정책 카탈로그(policies.ts)의 MVP 3종을 목록으로 보여주고,
 * 하나를 고르면 onSelect로 그 policy_id를 넘긴다. 판정·질문 라우팅은 상위(CheckFlow)가 담당한다.
 * 스타일은 최소한 — 디자인 반영은 #22.
 */
const STYLES = {
  container: "flex flex-col gap-3",
  heading: "text-lg font-medium",
  list: "flex flex-col gap-2",
  card: "flex flex-col gap-1 rounded border border-zinc-300 px-4 py-3 text-left hover:border-zinc-900",
  name: "font-medium",
  desc: "text-sm text-zinc-500",
};

export interface PolicySelectProps {
  onSelect: (policyId: PolicyId) => void;
}

export default function PolicySelect({ onSelect }: PolicySelectProps) {
  return (
    <div className={STYLES.container}>
      <p className={STYLES.heading}>어떤 정책을 확인해볼까요?</p>
      <div className={STYLES.list}>
        {POLICIES.map((policy) => (
          <button key={policy.id} type="button" className={STYLES.card} onClick={() => onSelect(policy.id)}>
            <span className={STYLES.name}>{policy.name}</span>
            <span className={STYLES.desc}>{policy.description}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
