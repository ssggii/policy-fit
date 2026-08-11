/**
 * 체크플로우 정책 카탈로그 — MVP 3정책(DOMAIN 5.1~5.3, `contracts/openapi.yaml` policy_id enum과 동일).
 * 여기 담긴 questionOrder가 정책별로 실제 물어볼 질문 키·순서를 정한다.
 * 판정 로직(rule DSL)은 백엔드 소유 — 이 파일은 화면에 보여줄 이름·설명·질문 순서만 다룬다.
 */

import type { QuestionKey } from "./questions";

export interface PolicyCatalogEntry {
  id: "jutaek-dream" | "beotimmok-jeonse" | "cheongnyeon-wolse";
  /** 정책 공식 명칭 */
  name: string;
  /** 목록에서 보여줄 한 줄 일상어 설명 (F-001) */
  description: string;
  /** 이 정책 판정에 필요한 질문 키 — 이 순서대로 CheckFlow가 질문한다 */
  questionOrder: QuestionKey[];
}

export const POLICIES: PolicyCatalogEntry[] = [
  {
    id: "jutaek-dream",
    name: "청년 주택드림 청약통장",
    description: "청약통장을 만들고 싶은 청년을 위한 제도예요.",
    questionOrder: ["age", "housing_none", "income_self"],
  },
  {
    id: "beotimmok-jeonse",
    name: "청년전용 버팀목 전세자금대출",
    description: "전세로 살고 있는 청년에게 낮은 금리로 대출해줘요.",
    questionOrder: ["age", "housing_none", "lease_type", "income_self", "asset_self", "married"],
  },
  {
    id: "cheongnyeon-wolse",
    name: "청년월세 특별지원",
    description: "월세로 살고 있는 청년에게 월세를 지원해줘요.",
    questionOrder: ["age", "housing_none", "lease_type", "income_self", "married"],
  },
];
