import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import ResultView from "./ResultView";
import type { VerdictResult } from "@/lib/types/verdict";

const baseReasoning: VerdictResult["reasoning"] = [
  { atom: "age", label: "나이", result: "met", detail: "19~34세", source: "국토교통부", year: 2026 },
  { atom: "housing_none", label: "무주택 여부", result: "met", source: "국토교통부", year: 2026 },
  { atom: "income_self", label: "본인 소득", result: "met", source: "국토교통부", year: 2026 },
];

const SELECTION_METHOD_FIXTURE = "선발 없음";

function buildResult(overrides: Partial<VerdictResult>): VerdictResult {
  return {
    policy_id: "jutaek-dream",
    verdict: { state: "eligible" },
    reasoning: baseReasoning,
    application: {
      url: "https://nhuf.molit.go.kr",
      selection_method: SELECTION_METHOD_FIXTURE,
      period: "상시",
    },
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
    expect(screen.getByText("판정 근거")).toBeInTheDocument();
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
    expect(screen.getByText("판정 근거")).toBeInTheDocument();
    // F-006: "가능 또는 추가 확인 필요" 정책엔 신청 채널 링크가 제공된다 — eligible만 아니라 needs_review도 커버.
    expect(screen.getByRole("link", { name: "공식 신청 페이지로 이동" })).toHaveAttribute(
      "href",
      "https://nhuf.molit.go.kr"
    );
    // F-007: 선발 방식 고지가 라벨뿐 아니라 '값까지' 화면에 그대로 렌더된다.
    // 값의 정확성(정책 데이터가 1차 출처와 맞는지)은 backend VerdictsControllerTest 담당 — 여기선 표시 경로만 가드한다.
    expect(screen.getByText("선발 방식").closest("p")).toHaveTextContent(SELECTION_METHOD_FIXTURE);
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

  it("out_of_scope: 자가판정 불가 뱃지 + 부적합과 구분 문구, 빈 근거 섹션·신청 링크 없음(F-003·F-007)", () => {
    // 백엔드는 out_of_scope에 근거 빈 배열·application 없음을 준다.
    render(
      <ResultView
        result={buildResult({ verdict: { state: "out_of_scope" }, reasoning: [], application: undefined })}
      />
    );

    expect(screen.getByText("자가판정 불가")).toBeInTheDocument();
    // F-007: '부적합'과 문구상 구분 — 시스템 한계임을 명시
    expect(screen.getByText(/부적합이 아니라 자가판정 불가예요/)).toBeInTheDocument();
    // 근거가 비면 "판정 근거" 섹션을 노출하지 않는다
    expect(screen.queryByText("판정 근거")).not.toBeInTheDocument();
    // 신청 링크 없음
    expect(screen.queryByRole("link", { name: "공식 신청 페이지로 이동" })).not.toBeInTheDocument();
    // F-005 고지는 유지
    expect(screen.getByText(/이 결과는 입력하신 내용을 바탕으로 한 추정 판정입니다/)).toBeInTheDocument();
  });

  it("out_of_scope 문구는 ineligible에는 나오지 않는다(부적합과 구분)", () => {
    render(<ResultView result={buildResult({ verdict: { state: "ineligible" }, application: undefined })} />);
    expect(screen.getByText("부적합")).toBeInTheDocument();
    expect(screen.queryByText(/부적합이 아니라 자가판정 불가예요/)).not.toBeInTheDocument();
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
