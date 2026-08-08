import { describe, expect, it } from "vitest";
import { toVerdictRequestAnswers } from "./answers";

describe("toVerdictRequestAnswers", () => {
  it("필드명이 다른 소득·재산을 계약 필드명으로 매핑한다", () => {
    const result = toVerdictRequestAnswers({
      income_self: { known: true, approx: false, value: 3000000 },
      asset_self: { known: true, approx: false, value: 100000000 },
    });
    expect(result.income_self_monthly_krw).toEqual({ known: true, approx: false, value: 3000000 });
    expect(result.asset_self_krw).toEqual({ known: true, approx: false, value: 100000000 });
  });

  it("키가 같은 필드는 그대로 옮긴다", () => {
    const result = toVerdictRequestAnswers({
      age: { known: true, value: 28 },
      housing_none: { known: true, value: true },
      lease_type: { known: true, value: "wolse" },
      married: { known: false },
    });
    expect(result.age).toEqual({ known: true, value: 28 });
    expect(result.housing_none).toEqual({ known: true, value: true });
    expect(result.lease_type).toEqual({ known: true, value: "wolse" });
    expect(result.married).toEqual({ known: false });
  });

  it("없는 답변은 결과에 포함하지 않는다", () => {
    const result = toVerdictRequestAnswers({ age: { known: true, value: 28 } });
    expect(result.income_self_monthly_krw).toBeUndefined();
    expect(Object.keys(result)).toEqual(["age"]);
  });
});
