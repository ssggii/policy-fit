/**
 * 체크플로우 질문 문구 (F-001: 일상어, 행정 용어 그대로 쓰지 않기).
 * MVP 3정책(DOMAIN 5.1~5.3)이 쓰는 원자를 모두 질문으로 갖고 있다 — 정책별로 필요한
 * 질문만 골라 순서대로 묻는 구성은 policies.ts(정책 카탈로그)가 담당한다.
 */

export type QuestionKey = "age" | "housing_none" | "income_self" | "lease_type" | "asset_self" | "married";

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

export interface SelectOption {
  /** AnswerString.value로 그대로 들어가는 값 (예: "jeonse") */
  value: string;
  /** 버튼에 보이는 문구 (일상어) */
  label: string;
}

export interface SelectQuestion extends BaseQuestion {
  type: "select";
  options: SelectOption[];
}

export type Question = IntQuestion | BoolQuestion | ApproxIntQuestion | SelectQuestion;

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
    label: "지금 본인 이름으로 된 집이 없으신가요?",
    helper: "전세나 월세로 사는 건 '집이 없음'에 해당해요.",
    trueLabel: "네, 없어요",
    falseLabel: "아니요, 있어요",
  },
  {
    key: "income_self",
    type: "approx_int",
    label: "본인의 월 소득은 얼마인가요?",
    helper: "세금 떼기 전 금액 기준이에요. 정확히 모르면 '대략'으로 답해도 괜찮아요.",
    unit: "원",
    placeholder: "예: 3000000",
  },
  {
    key: "lease_type",
    type: "select",
    label: "지금 사는 집은 전세인가요, 월세인가요?",
    helper: "계약서상 임차 형태를 골라주세요.",
    options: [
      { value: "jeonse", label: "전세" },
      { value: "wolse", label: "월세" },
    ],
  },
  {
    key: "asset_self",
    type: "approx_int",
    label: "본인 재산은 얼마나 되나요?",
    helper: "예금·부동산 등을 합친 금액이에요. 정확히 모르면 '대략'으로 답해도 괜찮아요.",
    unit: "원",
    placeholder: "예: 100000000",
  },
  {
    key: "married",
    type: "bool",
    label: "혼인 중이신가요?",
    helper: "법률상 혼인 상태를 기준으로 답해주세요.",
    trueLabel: "네, 혼인 중이에요",
    falseLabel: "아니요, 미혼이에요",
  },
];
