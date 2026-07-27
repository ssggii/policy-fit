/**
 * 청년 주택드림 청약통장 체크플로우 질문 문구 (F-001: 일상어, 행정 용어 그대로 쓰지 않기).
 * DOMAIN 5.1 — 이 정책은 age·housing_none·income_self 3개 원자만 쓴다(all_of, 전부 self).
 * 질문 순서 = 원자 요구값을 자연스러운 대화 순서로 배치한 것 (나이 → 무주택 → 소득).
 */

export type QuestionKey = "age" | "housing_none" | "income_self";

interface BaseQuestion {
  key: QuestionKey;
  /** 질문 문구 (일상어) */
  label: string;
  /** 보조 설명 — 행정 용어 풀이·응답 기준 안내 */
  helper: string;
}

export interface IntQuestion extends BaseQuestion {
  type: "int";
  unit: string;
  placeholder: string;
}

export interface BoolQuestion extends BaseQuestion {
  type: "bool";
  /** value=true를 고르는 버튼 문구 */
  trueLabel: string;
  /** value=false를 고르는 버튼 문구 */
  falseLabel: string;
}

export interface ApproxIntQuestion extends BaseQuestion {
  type: "approx_int";
  unit: string;
  placeholder: string;
}

export type Question = IntQuestion | BoolQuestion | ApproxIntQuestion;

export const QUESTIONS: Question[] = [
  {
    key: "age",
    type: "int",
    label: "나이가 어떻게 되세요?",
    helper: "만 나이로 답해주세요.",
    unit: "세",
    placeholder: "예: 28",
  },
  {
    key: "housing_none",
    type: "bool",
    label: "지금 본인 이름으로 된 집을 갖고 있나요?",
    helper: "전세나 월세로 사는 건 '집이 없음'에 해당해요.",
    trueLabel: "아니요, 없어요",
    falseLabel: "네, 있어요",
  },
  {
    key: "income_self",
    type: "approx_int",
    label: "본인의 월 소득은 얼마인가요?",
    helper: "세금 떼기 전 금액 기준이에요. 정확히 모르면 '대략'으로 답해도 괜찮아요.",
    unit: "원",
    placeholder: "예: 3000000",
  },
];
