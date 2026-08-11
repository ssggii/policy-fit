import { describe, expect, it } from "vitest";
import { POLICIES } from "./policies";
import { QUESTIONS } from "./questions";

describe("정책 카탈로그(POLICIES)", () => {
  it("MVP 3정책이 계약 policy_id 순서로 정의돼 있다", () => {
    expect(POLICIES.map((p) => p.id)).toEqual(["jutaek-dream", "beotimmok-jeonse", "cheongnyeon-wolse"]);
  });

  it("정책별 questionOrder가 DOMAIN 5.1~5.3 원자 구성과 일치한다", () => {
    const order = Object.fromEntries(POLICIES.map((p) => [p.id, p.questionOrder]));
    expect(order["jutaek-dream"]).toEqual(["age", "housing_none", "income_self"]);
    expect(order["beotimmok-jeonse"]).toEqual([
      "age",
      "housing_none",
      "lease_type",
      "income_self",
      "asset_self",
      "married",
    ]);
    expect(order["cheongnyeon-wolse"]).toEqual(["age", "housing_none", "lease_type", "income_self", "married"]);
  });

  it("모든 정책의 questionOrder는 정의된 질문 키만 참조한다", () => {
    const keys = new Set(QUESTIONS.map((q) => q.key));
    for (const policy of POLICIES) {
      for (const key of policy.questionOrder) {
        expect(keys.has(key)).toBe(true);
      }
    }
  });
});
