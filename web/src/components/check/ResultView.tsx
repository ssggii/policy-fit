import type { AtomResult, UnknownReason, VerdictResult, VerdictState } from "@/lib/types/verdict";
import Disclaimer from "./Disclaimer";

/**
 * state·atom result별 표시 문구·색상 맵. 와이어프레임 적용 시 이 맵의 className만 바꾸면 된다.
 * 상태색은 최소한만: 가능=초록, 부적합=회색, 추가 확인 필요=주황.
 */
const STATE_LABELS: Record<VerdictState, string> = {
  eligible: "가능",
  ineligible: "부적합",
  needs_review: "추가 확인 필요",
  out_of_scope: "자가판정 불가",
};

const STATE_BADGE_STYLES: Record<VerdictState, string> = {
  eligible: "bg-green-100 text-green-800 border border-green-300",
  ineligible: "bg-zinc-100 text-zinc-700 border border-zinc-300",
  needs_review: "bg-orange-100 text-orange-800 border border-orange-300",
  out_of_scope: "bg-zinc-100 text-zinc-700 border border-zinc-300",
};

const ATOM_RESULT_LABELS: Record<AtomResult, string> = {
  met: "충족",
  unmet: "미충족",
  unknown: "불확실",
};

const ATOM_RESULT_STYLES: Record<AtomResult, string> = {
  met: "text-green-700",
  unmet: "text-zinc-500",
  unknown: "text-orange-700",
};

/** DOMAIN 3.1 — unknown 출처별 안내 문구. */
const UNKNOWN_REASON_TEXT: Record<UnknownReason, string> = {
  input_uncertain: "값을 정확히 입력하면 판정이 확정돼요.",
  admin_discretion: "행정청 확인이 필요한 항목이 있어요.",
  boundary: "경계값 근처라 정확한 값이면 결과가 갈릴 수 있어요.",
};

const STYLES = {
  page: "flex flex-col gap-6",
  badge: "inline-block w-fit rounded-full px-4 py-1 text-sm font-medium",
  unknownNote: "text-sm text-orange-700",
  section: "flex flex-col gap-2",
  sectionTitle: "text-base font-medium",
  reasoningList: "flex flex-col gap-2",
  reasoningItem: "rounded border border-zinc-200 px-3 py-2",
  reasoningLabelRow: "flex items-center justify-between gap-2",
  reasoningLabel: "font-medium",
  reasoningResult: "text-sm font-medium",
  reasoningDetail: "mt-1 text-sm text-zinc-600",
  reasoningSource: "mt-1 text-xs text-zinc-400",
  applicationBox: "flex flex-col gap-2 rounded border border-zinc-200 px-4 py-3",
  applicationMeta: "text-sm text-zinc-600",
  handoffButton: "mt-1 self-start rounded bg-zinc-900 px-5 py-2 text-white",
};

export interface ResultViewProps {
  result: VerdictResult;
}

/**
 * 판정 결과 화면. verdict.state는 백엔드가 계산한 값을 그대로 표시만 한다(판정 로직 없음).
 */
export default function ResultView({ result }: ResultViewProps) {
  const { verdict, reasoning, application } = result;

  return (
    <div className={STYLES.page}>
      <div>
        <span className={`${STYLES.badge} ${STATE_BADGE_STYLES[verdict.state]}`}>{STATE_LABELS[verdict.state]}</span>
        {verdict.unknown_reasons && verdict.unknown_reasons.length > 0 && (
          <ul className="mt-2 flex flex-col gap-1">
            {verdict.unknown_reasons.map((reason) => (
              <li key={reason} className={STYLES.unknownNote}>
                {UNKNOWN_REASON_TEXT[reason]}
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* F-005: 3개 state 전부에서 조건 없이 항상 렌더링 */}
      <Disclaimer />

      <div className={STYLES.section}>
        <p className={STYLES.sectionTitle}>요건별 근거</p>
        <ul className={STYLES.reasoningList}>
          {reasoning.map((item) => (
            <li key={item.atom} className={STYLES.reasoningItem}>
              <div className={STYLES.reasoningLabelRow}>
                <span className={STYLES.reasoningLabel}>{item.label}</span>
                <span className={`${STYLES.reasoningResult} ${ATOM_RESULT_STYLES[item.result]}`}>
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
            </li>
          ))}
        </ul>
      </div>

      {/* F-006: application이 있는 경우에만(백엔드가 ineligible엔 생략) 핸드오프 버튼을 보여준다 */}
      {application?.url && (
        <div className={STYLES.applicationBox}>
          <p className={STYLES.sectionTitle}>신청 안내</p>
          {application.selection_method && <p className={STYLES.applicationMeta}>선발 방식: {application.selection_method}</p>}
          {application.period && <p className={STYLES.applicationMeta}>신청 시기: {application.period}</p>}
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
