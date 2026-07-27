import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import ResultView from "./ResultView";
import type { VerdictResult } from "@/lib/types/verdict";

const baseReasoning: VerdictResult["reasoning"] = [
  { atom: "age", label: "나이", result: "met", detail: "19~34세", source: "국토교통부", year: 2026 },
  { atom: "housing_none", label: "무주택 여부", result: "met", source: "국토교통부", year: 2026 },
  { atom: "income_self", label: "본인 소득", result: "met", source: "국토교통부", year: 2026 },
];

function buildResult(overrides: Partial<VerdictResult>): VerdictResult {
  return {
    policy_id: "jutaek-dream",
    verdict: { state: "eligible" },
    reasoning: baseReasoning,
    application: { url: "https://nhuf.molit.go.kr", selection_method: "선발 없음", period: "상시" },
    ...overrides,
  };
}

describe("ResultView", () => {
  it("eligible: 가능 뱃지 + 신청 링크가 보인다", () => {
    render(<ResultView result={buildResult({ verdict: { state: "eligible" } })} />);

    expect(screen.getByText("가능")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "공식 신청 페이지로 이동" })).toHaveAttribute(
      "href",
      "https://nhuf.molit.go.kr"
    );
  });

  it("ineligible: 부적합 뱃지 + 신청 링크 없음", () => {
    render(
      <ResultView
        result={buildResult({ verdict: { state: "ineligible" }, application: undefined })}
      />
    );

    expect(screen.getByText("부적합")).toBeInTheDocument();
    expect(screen.queryByRole("link", { name: "공식 신청 페이지로 이동" })).not.toBeInTheDocument();
  });

  it("needs_review: 추가 확인 필요 뱃지 + unknown_reasons 안내 문구 + 신청 링크 노출(F-006)", () => {
    render(
      <ResultView
        result={buildResult({
          verdict: { state: "needs_review", unknown_reasons: ["input_uncertain"] },
        })}
      />
    );

    expect(screen.getByText("추가 확인 필요")).toBeInTheDocument();
    expect(screen.getByText("값을 정확히 입력하면 판정이 확정돼요.")).toBeInTheDocument();
    // F-006: "가능 또는 추가 확인 필요" 정책엔 신청 채널 링크가 제공된다 — eligible만 아니라 needs_review도 커버.
    expect(screen.getByRole("link", { name: "공식 신청 페이지로 이동" })).toHaveAttribute(
      "href",
      "https://nhuf.molit.go.kr"
    );
  });

  it("3개 state 모두에서 추정 고지가 항상 보인다", () => {
    (["eligible", "ineligible", "needs_review"] as const).forEach((state) => {
      const { unmount } = render(
        <ResultView
          result={buildResult({
            verdict: state === "needs_review" ? { state, unknown_reasons: ["input_uncertain"] } : { state },
            application: state === "ineligible" ? undefined : buildResult({}).application,
          })}
        />
      );

      expect(
        screen.getByText(/이 결과는 입력하신 내용을 바탕으로 한 추정 판정입니다/)
      ).toBeInTheDocument();
      unmount();
    });
  });

  it("요건별 근거가 충족/미충족/불확실로 구분되어 표시된다", () => {
    render(
      <ResultView
        result={buildResult({
          reasoning: [
            { atom: "age", label: "나이", result: "met" },
            { atom: "housing_none", label: "무주택 여부", result: "unmet" },
            { atom: "income_self", label: "본인 소득", result: "unknown" },
          ],
        })}
      />
    );

    expect(screen.getByText("충족")).toBeInTheDocument();
    expect(screen.getByText("미충족")).toBeInTheDocument();
    expect(screen.getByText("불확실")).toBeInTheDocument();
  });
});
