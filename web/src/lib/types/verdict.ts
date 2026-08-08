/**
 * `contracts/openapi.yaml` · `contracts/verdict.schema.json` 기준 수동 TS 타입.
 * contracts는 읽기 전용 SSOT — 계약이 바뀌면 이 파일도 사람 승인 후 갱신한다.
 * (계약 코드젠 도구는 아직 붙어있지 않음 — CLAUDE.md 참고)
 */

/** 정수 값 질문의 답 (예: 나이). known=false면 "모름" — value 생략. */
export interface AnswerInt {
  known: boolean;
  value?: number;
}

/** 참/거짓 질문의 답 (예: 무주택 여부). known=false면 "모름" — value 생략. */
export interface AnswerBool {
  known: boolean;
  value?: boolean;
}

/** "대략" 응답이 가능한 정수 값 질문의 답 (예: 본인 소득). */
export interface AnswerApproxInt {
  known: boolean;
  approx?: boolean;
  value?: number;
}

/** 문자열 값 질문의 답 (예: 임차 형태 "jeonse"/"wolse"). known=false면 "모름" — value 생략. */
export interface AnswerString {
  known: boolean;
  value?: string;
}

/** MVP 범위 정책 id 3종. openapi enum과 동일하게 유지한다. */
export type PolicyId = "jutaek-dream" | "beotimmok-jeonse" | "cheongnyeon-wolse";

export interface VerdictRequestAnswers {
  age?: AnswerInt;
  housing_none?: AnswerBool;
  income_self_monthly_krw?: AnswerApproxInt;
  lease_type?: AnswerString;
  asset_self_krw?: AnswerApproxInt;
  married?: AnswerBool;
}

export interface VerdictRequest {
  policy_id: PolicyId;
  answers: VerdictRequestAnswers;
}

/** DOMAIN 1.1 4-state. out_of_scope는 이 화면(F-003)엔 등장하지 않지만 타입엔 포함해 방어적으로 다룬다. */
export type VerdictState = "eligible" | "ineligible" | "needs_review" | "out_of_scope";

/** DOMAIN 3.1 unknown 출처 태그. needs_review일 때만 존재. */
export type UnknownReason = "input_uncertain" | "admin_discretion" | "boundary";

export interface Verdict {
  state: VerdictState;
  unknown_reasons?: UnknownReason[];
}

export type AtomResult = "met" | "unmet" | "unknown";

export interface AtomReasoning {
  atom: string;
  label: string;
  result: AtomResult;
  detail?: string;
  source?: string;
  year?: number;
}

export interface PolicyApplication {
  url?: string;
  selection_method?: string;
  period?: string;
}

export interface VerdictResult {
  policy_id: string;
  verdict: Verdict;
  reasoning: AtomReasoning[];
  /** F-006: ineligible이면 null 또는 생략될 수 있다. */
  application?: PolicyApplication | null;
}

export interface ErrorResponse {
  message: string;
}
