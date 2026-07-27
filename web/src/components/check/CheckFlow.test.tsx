import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import CheckFlow from "./CheckFlow";
import { postVerdict } from "@/lib/api/verdicts";
import type { VerdictRequest, VerdictResult } from "@/lib/types/verdict";

vi.mock("@/lib/api/verdicts", () => ({
  postVerdict: vi.fn(),
  VerdictApiError: class VerdictApiError extends Error {},
}));

const mockedPostVerdict = vi.mocked(postVerdict);

const eligibleResult: VerdictResult = {
  policy_id: "jutaek-dream",
  verdict: { state: "eligible" },
  reasoning: [{ atom: "age", label: "나이", result: "met" }],
  application: { url: "https://nhuf.molit.go.kr" },
};

async function answerAge(user: ReturnType<typeof userEvent.setup>, value: string) {
  await user.type(screen.getByRole("spinbutton"), value);
  await user.click(screen.getByRole("button", { name: "다음" }));
}

describe("CheckFlow", () => {
  beforeEach(() => {
    mockedPostVerdict.mockReset();
  });

  it("age → housing_none → income_self 순서로 질문이 전환된다", async () => {
    mockedPostVerdict.mockResolvedValue(eligibleResult);
    const user = userEvent.setup();
    render(<CheckFlow />);

    expect(screen.getByText("나이가 어떻게 되세요?")).toBeInTheDocument();
    await answerAge(user, "28");

    expect(screen.getByText("지금 본인 이름으로 된 집을 갖고 있나요?")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "아니요, 없어요" }));
    await user.click(screen.getByRole("button", { name: "다음" }));

    expect(screen.getByText("본인의 월 소득은 얼마인가요?")).toBeInTheDocument();
  });

  it("마지막 질문 응답 시 postVerdict를 정확한 payload로 1회 호출한다", async () => {
    mockedPostVerdict.mockResolvedValue(eligibleResult);
    const user = userEvent.setup();
    render(<CheckFlow />);

    await answerAge(user, "28");
    await user.click(screen.getByRole("button", { name: "아니요, 없어요" }));
    await user.click(screen.getByRole("button", { name: "다음" }));
    await user.type(screen.getByRole("spinbutton"), "3000000");
    await user.click(screen.getByRole("button", { name: "다음" }));

    await waitFor(() => expect(mockedPostVerdict).toHaveBeenCalledTimes(1));

    const requestArg = mockedPostVerdict.mock.calls[0][0] as VerdictRequest;
    expect(requestArg).toEqual({
      policy_id: "jutaek-dream",
      answers: {
        age: { known: true, value: 28 },
        housing_none: { known: true, value: true },
        income_self_monthly_krw: { known: true, approx: false, value: 3000000 },
      },
    });
  });

  it("응답을 받으면 결과 화면(뱃지)이 렌더링된다", async () => {
    mockedPostVerdict.mockResolvedValue(eligibleResult);
    const user = userEvent.setup();
    render(<CheckFlow />);

    await answerAge(user, "28");
    await user.click(screen.getByRole("button", { name: "아니요, 없어요" }));
    await user.click(screen.getByRole("button", { name: "다음" }));
    await user.type(screen.getByRole("spinbutton"), "3000000");
    await user.click(screen.getByRole("button", { name: "다음" }));

    expect(await screen.findByText("가능")).toBeInTheDocument();
  });

  it("이전 질문으로 버튼으로 뒤로 갈 수 있고 답변이 유지된다", async () => {
    const user = userEvent.setup();
    render(<CheckFlow />);

    await answerAge(user, "28");
    expect(screen.getByText("지금 본인 이름으로 된 집을 갖고 있나요?")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "이전 질문으로" }));
    expect(screen.getByText("나이가 어떻게 되세요?")).toBeInTheDocument();
    expect(screen.getByRole("spinbutton")).toHaveValue(28);
  });
});
