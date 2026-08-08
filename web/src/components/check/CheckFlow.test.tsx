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

type User = ReturnType<typeof userEvent.setup>;

async function selectPolicy(user: User, name: string) {
  await user.click(screen.getByRole("button", { name: new RegExp(name) }));
}

/** number 입력 질문(나이·소득·재산): 값 입력 후 '다음'. */
async function answerNumber(user: User, value: string) {
  await user.type(screen.getByRole("spinbutton"), value);
  await user.click(screen.getByRole("button", { name: "다음" }));
}

/** 버튼 선택 질문(무주택·임차형태): 옵션 클릭 후 '다음'. */
async function chooseOption(user: User, name: string) {
  await user.click(screen.getByRole("button", { name }));
  await user.click(screen.getByRole("button", { name: "다음" }));
}

describe("CheckFlow — 정책 선택", () => {
  beforeEach(() => mockedPostVerdict.mockReset());

  it("처음엔 정책 선택 화면을 보여준다", () => {
    render(<CheckFlow />);
    expect(screen.getByText("어떤 정책을 확인해볼까요?")).toBeInTheDocument();
  });

  it("정책을 고르면 그 정책의 첫 질문으로 넘어간다", async () => {
    const user = userEvent.setup();
    render(<CheckFlow />);
    await selectPolicy(user, "청년 주택드림 청약통장");
    expect(screen.getByText("나이가 어떻게 되세요?")).toBeInTheDocument();
  });
});

describe("CheckFlow — 청년 주택드림(jutaek-dream)", () => {
  beforeEach(() => mockedPostVerdict.mockReset());

  it("age → housing_none → income_self 순서로 질문이 전환된다", async () => {
    mockedPostVerdict.mockResolvedValue(eligibleResult);
    const user = userEvent.setup();
    render(<CheckFlow />);
    await selectPolicy(user, "청년 주택드림 청약통장");

    expect(screen.getByText("나이가 어떻게 되세요?")).toBeInTheDocument();
    await answerNumber(user, "28");

    expect(screen.getByText("지금 본인 이름으로 된 집이 없으신가요?")).toBeInTheDocument();
    await chooseOption(user, "네, 없어요");

    expect(screen.getByText("본인의 월 소득은 얼마인가요?")).toBeInTheDocument();
  });

  it("마지막 질문 응답 시 postVerdict를 정확한 payload로 1회 호출한다", async () => {
    mockedPostVerdict.mockResolvedValue(eligibleResult);
    const user = userEvent.setup();
    render(<CheckFlow />);
    await selectPolicy(user, "청년 주택드림 청약통장");

    await answerNumber(user, "28");
    await chooseOption(user, "네, 없어요");
    await answerNumber(user, "3000000");

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
    await selectPolicy(user, "청년 주택드림 청약통장");

    await answerNumber(user, "28");
    await chooseOption(user, "네, 없어요");
    await answerNumber(user, "3000000");

    expect(await screen.findByText("가능")).toBeInTheDocument();
  });

  it("이전 질문으로 버튼으로 뒤로 갈 수 있고 답변이 유지된다", async () => {
    const user = userEvent.setup();
    render(<CheckFlow />);
    await selectPolicy(user, "청년 주택드림 청약통장");

    await answerNumber(user, "28");
    expect(screen.getByText("지금 본인 이름으로 된 집이 없으신가요?")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "이전 질문으로" }));
    expect(screen.getByText("나이가 어떻게 되세요?")).toBeInTheDocument();
    expect(screen.getByRole("spinbutton")).toHaveValue(28);
  });

  it("첫 질문에서 '정책 다시 고르기'로 선택 화면에 돌아간다", async () => {
    const user = userEvent.setup();
    render(<CheckFlow />);
    await selectPolicy(user, "청년 주택드림 청약통장");
    expect(screen.getByText("나이가 어떻게 되세요?")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "정책 다시 고르기" }));
    expect(screen.getByText("어떤 정책을 확인해볼까요?")).toBeInTheDocument();
  });
});

describe("CheckFlow — 청년전용 버팀목(beotimmok-jeonse)", () => {
  beforeEach(() => mockedPostVerdict.mockReset());

  it("lease_type·asset_self를 포함한 정책별 질문·payload를 구성한다", async () => {
    mockedPostVerdict.mockResolvedValue({ ...eligibleResult, policy_id: "beotimmok-jeonse" });
    const user = userEvent.setup();
    render(<CheckFlow />);
    await selectPolicy(user, "청년전용 버팀목 전세자금대출");

    // age → housing_none → lease_type(select) → income_self → asset_self
    await answerNumber(user, "28");
    await chooseOption(user, "네, 없어요");
    expect(screen.getByText("지금 사는 집은 전세인가요, 월세인가요?")).toBeInTheDocument();
    await chooseOption(user, "전세");
    await answerNumber(user, "3000000");
    await answerNumber(user, "100000000");

    await waitFor(() => expect(mockedPostVerdict).toHaveBeenCalledTimes(1));
    const requestArg = mockedPostVerdict.mock.calls[0][0] as VerdictRequest;
    expect(requestArg).toEqual({
      policy_id: "beotimmok-jeonse",
      answers: {
        age: { known: true, value: 28 },
        housing_none: { known: true, value: true },
        lease_type: { known: true, value: "jeonse" },
        income_self_monthly_krw: { known: true, approx: false, value: 3000000 },
        asset_self_krw: { known: true, approx: false, value: 100000000 },
      },
    });
  });
});
