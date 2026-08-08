import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import QuestionStep from "./QuestionStep";
import { QUESTIONS, type BoolQuestion } from "./questions";

const ageQuestion = QUESTIONS.find((q) => q.key === "age")!;
const housingQuestion = QUESTIONS.find((q) => q.key === "housing_none")! as BoolQuestion;
const incomeQuestion = QUESTIONS.find((q) => q.key === "income_self")!;
const leaseQuestion = QUESTIONS.find((q) => q.key === "lease_type")!;

describe("QuestionStep — int (age)", () => {
  it("모름 체크 시 known:false로 제출된다", async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<QuestionStep question={ageQuestion} onSubmit={onSubmit} />);

    await user.click(screen.getByRole("checkbox", { name: "모름" }));
    await user.click(screen.getByRole("button", { name: "다음" }));

    expect(onSubmit).toHaveBeenCalledWith({ known: false });
  });

  it("모름 체크 시 입력창이 비활성화된다", async () => {
    const user = userEvent.setup();
    render(<QuestionStep question={ageQuestion} onSubmit={vi.fn()} />);

    await user.click(screen.getByRole("checkbox", { name: "모름" }));

    expect(screen.getByRole("spinbutton")).toBeDisabled();
  });

  it("숫자를 입력하면 known:true, value로 제출된다", async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<QuestionStep question={ageQuestion} onSubmit={onSubmit} />);

    await user.type(screen.getByRole("spinbutton"), "28");
    await user.click(screen.getByRole("button", { name: "다음" }));

    expect(onSubmit).toHaveBeenCalledWith({ known: true, value: 28 });
  });

  it("아무 값도 없으면 다음 버튼이 비활성화된다", () => {
    render(<QuestionStep question={ageQuestion} onSubmit={vi.fn()} />);
    expect(screen.getByRole("button", { name: "다음" })).toBeDisabled();
  });
});

describe("QuestionStep — bool (housing_none)", () => {
  it("trueLabel 선택 시 value:true로 제출된다", async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<QuestionStep question={housingQuestion} onSubmit={onSubmit} />);

    await user.click(screen.getByRole("button", { name: housingQuestion.trueLabel }));
    await user.click(screen.getByRole("button", { name: "다음" }));

    expect(onSubmit).toHaveBeenCalledWith({ known: true, value: true });
  });

  it("falseLabel 선택 시 value:false로 제출된다", async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<QuestionStep question={housingQuestion} onSubmit={onSubmit} />);

    await user.click(screen.getByRole("button", { name: housingQuestion.falseLabel }));
    await user.click(screen.getByRole("button", { name: "다음" }));

    expect(onSubmit).toHaveBeenCalledWith({ known: true, value: false });
  });

  it("모름 선택 시 known:false로 제출된다", async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<QuestionStep question={housingQuestion} onSubmit={onSubmit} />);

    await user.click(screen.getByRole("button", { name: "모름" }));
    await user.click(screen.getByRole("button", { name: "다음" }));

    expect(onSubmit).toHaveBeenCalledWith({ known: false });
  });
});

describe("QuestionStep — approx_int (income_self)", () => {
  it("대략 체크 후 값 입력 시 approx:true로 제출된다", async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<QuestionStep question={incomeQuestion} onSubmit={onSubmit} />);

    await user.click(screen.getByRole("checkbox", { name: /대략 알아요/ }));
    await user.type(screen.getByRole("spinbutton"), "3000000");
    await user.click(screen.getByRole("button", { name: "다음" }));

    expect(onSubmit).toHaveBeenCalledWith({ known: true, approx: true, value: 3000000 });
  });

  it("대략 체크 없이 값 입력 시 approx:false로 제출된다", async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<QuestionStep question={incomeQuestion} onSubmit={onSubmit} />);

    await user.type(screen.getByRole("spinbutton"), "3000000");
    await user.click(screen.getByRole("button", { name: "다음" }));

    expect(onSubmit).toHaveBeenCalledWith({ known: true, approx: false, value: 3000000 });
  });

  it("모름 체크 시 known:false로 제출된다 (대략 여부 무시)", async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<QuestionStep question={incomeQuestion} onSubmit={onSubmit} />);

    await user.click(screen.getByRole("checkbox", { name: "모름" }));
    await user.click(screen.getByRole("button", { name: "다음" }));

    expect(onSubmit).toHaveBeenCalledWith({ known: false });
  });
});

describe("QuestionStep — select (lease_type)", () => {
  it("옵션 선택 시 known:true, value로 제출된다", async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<QuestionStep question={leaseQuestion} onSubmit={onSubmit} />);

    await user.click(screen.getByRole("button", { name: "전세" }));
    await user.click(screen.getByRole("button", { name: "다음" }));

    expect(onSubmit).toHaveBeenCalledWith({ known: true, value: "jeonse" });
  });

  it("모름 선택 시 known:false로 제출된다", async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<QuestionStep question={leaseQuestion} onSubmit={onSubmit} />);

    await user.click(screen.getByRole("button", { name: "모름" }));
    await user.click(screen.getByRole("button", { name: "다음" }));

    expect(onSubmit).toHaveBeenCalledWith({ known: false });
  });

  it("아무것도 고르지 않으면 다음 버튼이 비활성화된다", () => {
    render(<QuestionStep question={leaseQuestion} onSubmit={vi.fn()} />);
    expect(screen.getByRole("button", { name: "다음" })).toBeDisabled();
  });
});
