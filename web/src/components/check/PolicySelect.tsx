"use client";

import type { PolicyId } from "@/lib/types/verdict";
import { POLICIES } from "./policies";

/**
 * 어떤 정책을 판정할지 고르는 화면. 정책 카탈로그(policies.ts)의 MVP 3종을 목록으로 보여주고,
 * 하나를 고르면 onSelect로 그 policy_id를 넘긴다. 판정·질문 라우팅은 상위(CheckFlow)가 담당한다.
 * 디자인: docs/design/polfit-design.html의 "정책 선택" 화면(pcard) — 모바일 1열, lg에서 3열 그리드(max 880).
 */
const STYLES = {
  container: "mx-auto flex w-full max-w-[520px] flex-col gap-6 lg:max-w-[880px]",
  heading: "flex flex-col gap-1.5 lg:items-center lg:text-center",
  headingTitle: "text-2xl font-extrabold tracking-[-0.035em] text-ink lg:text-[26px]",
  headingSub: "text-[13px] text-muted",
  list: "grid grid-cols-1 gap-3 lg:grid-cols-3 lg:gap-3.5",
  card: "hover-lift transition-app flex flex-col gap-1.5 rounded-2xl border-[1.5px] border-line bg-surface p-4 text-left hover:border-blue",
  nameRow: "flex items-center justify-between gap-2",
  name: "text-[15.5px] font-extrabold tracking-[-0.02em] text-ink",
  arrow: "font-extrabold text-blue",
  desc: "text-[12.5px] leading-relaxed text-muted",
};

export interface PolicySelectProps {
  onSelect: (policyId: PolicyId) => void;
}

export default function PolicySelect({ onSelect }: PolicySelectProps) {
  return (
    <div className={STYLES.container}>
      <div className={STYLES.heading}>
        <h1 className={STYLES.headingTitle}>어떤 정책을 확인해볼까요?</h1>
        <p className={STYLES.headingSub}>궁금한 정책을 골라 몇 가지 질문에 답하면 돼요.</p>
      </div>
      <div className={STYLES.list}>
        {POLICIES.map((policy) => (
          <button key={policy.id} type="button" className={STYLES.card} onClick={() => onSelect(policy.id)}>
            <div className={STYLES.nameRow}>
              <span className={STYLES.name}>{policy.name}</span>
              <span className={STYLES.arrow} aria-hidden>
                →
              </span>
            </div>
            <span className={STYLES.desc}>{policy.description}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
