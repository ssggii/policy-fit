import type { AtomResult, UnknownReason, VerdictResult, VerdictState } from "@/lib/types/verdict";
import Disclaimer from "./Disclaimer";

/**
 * state·atom result별 표시 문구·색상 맵. 디자인은 이 맵만 바꾸면 된다.
 * 4-state 판정 의미색(docs/design/polfit-design.html §02): eligible=green, needs_review=amber,
 * ineligible=slate, out_of_scope=violet. 액센트(blue)와는 겹치지 않는다.
 */
const STATE_LABELS: Record<VerdictState, string> = {
  eligible: "가능",
  ineligible: "부적합",
  needs_review: "추가 확인 필요",
  out_of_scope: "자가판정 불가",
};

const STATE_HERO_BG: Record<VerdictState, string> = {
  eligible: "bg-eligible-wash",
  ineligible: "bg-ineligible-wash",
  needs_review: "bg-review-wash",
  out_of_scope: "bg-oos-wash",
};

const STATE_TEXT: Record<VerdictState, string> = {
  eligible: "text-eligible",
  ineligible: "text-ineligible",
  needs_review: "text-review",
  out_of_scope: "text-oos",
};

const STATE_DOT_BG: Record<VerdictState, string> = {
  eligible: "bg-eligible",
  ineligible: "bg-ineligible",
  needs_review: "bg-review",
  out_of_scope: "bg-oos",
};

const ATOM_RESULT_LABELS: Record<AtomResult, string> = {
  met: "충족",
  unmet: "미충족",
  unknown: "불확실",
};

/** 근거 행의 원형 아이콘 배경 — met/unmet/unknown을 색으로도 구분한다(rrow .ic). */
const ATOM_ICON_BG: Record<AtomResult, string> = {
  met: "bg-eligible",
  unmet: "bg-ineligible",
  unknown: "bg-review",
};

const ATOM_ICON_GLYPH: Record<AtomResult, string> = {
  met: "✓",
  unmet: "✕",
  unknown: "?",
};

const ATOM_RESULT_TEXT: Record<AtomResult, string> = {
  met: "text-eligible",
  unmet: "text-ineligible",
  unknown: "text-review",
};

/** DOMAIN 3.1 — unknown 출처별 안내 문구. */
const UNKNOWN_REASON_TEXT: Record<UnknownReason, string> = {
  input_uncertain: "값을 정확히 입력하면 판정이 확정돼요.",
  admin_discretion: "행정청 확인이 필요한 항목이 있어요.",
  boundary: "경계값 근처라 정확한 값이면 결과가 갈릴 수 있어요.",
};

const STYLES = {
  page: "mx-auto flex w-full max-w-[560px] flex-col gap-6",
  hero: "flex flex-col gap-3 rounded-card p-5",
  badge: "inline-flex w-fit items-center gap-2 text-[28px] font-extrabold tracking-[-0.035em]",
  badgeDot: "h-2 w-2 flex-none rounded-full",
  unknownList: "flex flex-col gap-1",
  unknownNote: "text-[13px] leading-relaxed text-review",
  oosNote: "text-[13px] leading-relaxed text-muted",
  section: "flex flex-col gap-2.5",
  sectionTitle: "text-xs font-bold uppercase tracking-[0.1em] text-faint",
  reasoningList: "flex flex-col gap-2",
  reasoningItem: "flex items-center gap-3 rounded-xl border border-line bg-surface px-3.5 py-3",
  reasoningIcon: "grid h-5 w-5 flex-none place-items-center rounded-full text-[11px] font-extrabold text-on-blue",
  reasoningBody: "flex min-w-0 flex-1 flex-col gap-0.5",
  reasoningLabelRow: "flex items-center justify-between gap-2",
  reasoningLabel: "text-sm font-bold text-ink",
  reasoningResult: "flex-none text-xs font-bold",
  reasoningDetail: "text-xs text-muted",
  reasoningSource: "font-mono text-[10.5px] text-faint",
  applicationBox: "flex flex-col gap-2 rounded-2xl border border-line bg-surface p-4",
  applicationMeta: "text-[12.5px] leading-relaxed text-muted",
  applicationMetaLabel: "font-semibold text-ink",
  handoffButton:
    "mt-1 flex w-full items-center justify-center gap-2 rounded-input bg-blue px-[15px] py-[15px] text-base font-bold text-on-blue transition-app hover:bg-blue-press",
};

export interface ResultViewProps {
  result: VerdictResult;
}

/**
 * 판정 결과 화면. verdict.state는 백엔드가 계산한 값을 그대로 표시만 한다(판정 로직 없음).
 * 디자인: docs/design/polfit-design.html "결과" 화면(verdict-hero + rlist + app-card).
 */
export default function ResultView({ result }: ResultViewProps) {
  const { verdict, reasoning, application } = result;
  const state = verdict.state;

  return (
    <div className={STYLES.page}>
      <div className={`${STYLES.hero} ${STATE_HERO_BG[state]}`}>
        <h1 className={`${STYLES.badge} ${STATE_TEXT[state]}`}>
          <span className={`${STYLES.badgeDot} ${STATE_DOT_BG[state]}`} aria-hidden />
          {STATE_LABELS[state]}
        </h1>
        {/* F-003·F-007: out_of_scope는 사용자 자격 결과(3-state)가 아니라 시스템 범위 한계임을
            문구로 '부적합'과 구분한다. */}
        {state === "out_of_scope" && (
          <p className={STYLES.oosNote}>
            가구 합산·행정청 판단이 필요해 앱이 대신 판정하기 어려운 정책이에요. 부적합이 아니라 자가판정 불가예요 — 직접 확인하면 자격이 있을 수 있어요.
          </p>
        )}
        {verdict.unknown_reasons && verdict.unknown_reasons.length > 0 && (
          <ul className={STYLES.unknownList}>
            {verdict.unknown_reasons.map((reason) => (
              <li key={reason} className={STYLES.unknownNote}>
                {UNKNOWN_REASON_TEXT[reason]}
              </li>
            ))}
          </ul>
        )}

        {/* F-005: 4개 state 전부에서 조건 없이 항상 렌더링 */}
        <Disclaimer />
      </div>

      {/* out_of_scope는 백엔드가 근거를 빈 배열로 주므로 이 섹션을 숨긴다(빈 "판정 근거" 헤딩 방지). */}
      {reasoning.length > 0 && (
        <div className={STYLES.section}>
          <p className={STYLES.sectionTitle}>판정 근거</p>
          <ul className={STYLES.reasoningList}>
            {reasoning.map((item) => (
              <li key={item.atom} className={STYLES.reasoningItem}>
                <span className={`${STYLES.reasoningIcon} ${ATOM_ICON_BG[item.result]}`} aria-hidden>
                  {ATOM_ICON_GLYPH[item.result]}
                </span>
                <div className={STYLES.reasoningBody}>
                  <div className={STYLES.reasoningLabelRow}>
                    <span className={STYLES.reasoningLabel}>{item.label}</span>
                    <span className={`${STYLES.reasoningResult} ${ATOM_RESULT_TEXT[item.result]}`}>
                      {ATOM_RESULT_LABELS[item.result]}
                    </span>
                  </div>
                  {item.detail && <p className={STYLES.reasoningDetail}>{item.detail}</p>}
                  {(item.source || item.year) && (
                    <p className={STYLES.reasoningSource}>
                      출처: {item.source ?? "-"}
                      {item.year ? ` (${item.year}년 기준)` : ""}
                    </p>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* F-006: application이 있는 경우에만(백엔드가 ineligible엔 생략) 핸드오프 버튼을 보여준다 */}
      {application?.url && (
        <div className={STYLES.applicationBox}>
          <p className={STYLES.sectionTitle}>신청 안내</p>
          {application.selection_method && (
            <p className={STYLES.applicationMeta}>
              <span className={STYLES.applicationMetaLabel}>선발 방식</span> {application.selection_method}
            </p>
          )}
          {application.period && (
            <p className={STYLES.applicationMeta}>
              <span className={STYLES.applicationMetaLabel}>신청 시기</span> {application.period}
            </p>
          )}
          <a
            className={STYLES.handoffButton}
            href={application.url}
            target="_blank"
            rel="noopener noreferrer"
            onClick={() => trackHandoffClick(result)}
          >
            공식 신청 페이지로 이동
          </a>
        </div>
      )}
    </div>
  );
}

/**
 * F-006: 신청 전환 계측점. 무인증·미저장 원칙상 익명 이벤트만 남긴다(PII 없음).
 * 실제 계측 도구(analytics)는 아직 붙어있지 않아 콘솔 로그로 자리만 잡아둔다.
 */
function trackHandoffClick(result: VerdictResult) {
  console.info("[event] handoff_click", { policy_id: result.policy_id, state: result.verdict.state });
}
