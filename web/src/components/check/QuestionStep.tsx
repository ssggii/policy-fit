"use client";

import { useState } from "react";
import type { AnswerApproxInt, AnswerBool, AnswerInt } from "@/lib/types/verdict";
import type { ApproxIntQuestion, BoolQuestion, IntQuestion, Question } from "./questions";

/**
 * 스타일은 여기 한 곳에 상수로 몰아둔다 — 와이어프레임이 나오면 이 맵만 교체하면 되게.
 * 로직(상태·이벤트 핸들러)은 아래 컴포넌트 본문에 두고 className 문자열만 여기서 가져다 쓴다.
 */
const STYLES = {
  container: "flex flex-col gap-3",
  label: "text-lg font-medium",
  helper: "text-sm text-zinc-500",
  input: "w-full max-w-xs rounded border border-zinc-300 px-3 py-2 text-base disabled:bg-zinc-100 disabled:text-zinc-400",
  unit: "text-sm text-zinc-500",
  inputRow: "flex items-center gap-2",
  checkboxRow: "flex flex-wrap items-center gap-4 text-sm text-zinc-600",
  checkboxLabel: "flex items-center gap-1.5",
  optionRow: "flex flex-wrap gap-2",
  optionButtonBase: "rounded border px-4 py-2 text-sm",
  optionButtonSelected: "border-zinc-900 bg-zinc-900 text-white",
  optionButtonUnselected: "border-zinc-300 bg-white text-zinc-700",
  nextButton: "mt-2 self-start rounded bg-zinc-900 px-5 py-2 text-white disabled:bg-zinc-300",
};

type AnyAnswer = AnswerInt | AnswerBool | AnswerApproxInt;

export interface QuestionStepProps {
  question: Question;
  /** 뒤로가기 등으로 이 질문에 이미 답한 적이 있으면 초기값으로 채운다. */
  defaultAnswer?: AnyAnswer;
  onSubmit: (answer: AnyAnswer) => void;
}

/**
 * 질문 1개를 보여준다. question.type에 따라 int(나이) / bool(무주택) / approx_int(소득) 중
 * 하나의 입력 UI를 렌더링한다. 모든 값은 '모름'으로 응답 가능(F-001 수용 기준).
 */
export default function QuestionStep({ question, defaultAnswer, onSubmit }: QuestionStepProps) {
  if (question.type === "bool") {
    return (
      <BoolQuestionBody question={question} defaultAnswer={defaultAnswer as AnswerBool | undefined} onSubmit={onSubmit} />
    );
  }
  if (question.type === "approx_int") {
    return (
      <ApproxIntQuestionBody
        question={question}
        defaultAnswer={defaultAnswer as AnswerApproxInt | undefined}
        onSubmit={onSubmit}
      />
    );
  }
  return <IntQuestionBody question={question} defaultAnswer={defaultAnswer as AnswerInt | undefined} onSubmit={onSubmit} />;
}

function IntQuestionBody({
  question,
  defaultAnswer,
  onSubmit,
}: {
  question: IntQuestion;
  defaultAnswer?: AnswerInt;
  onSubmit: (answer: AnswerInt) => void;
}) {
  const [text, setText] = useState(defaultAnswer?.known && defaultAnswer.value !== undefined ? String(defaultAnswer.value) : "");
  const [unknown, setUnknown] = useState(defaultAnswer?.known === false);

  const parsed = Number(text);
  const hasValidNumber = text.trim() !== "" && Number.isFinite(parsed);
  const canSubmit = unknown || hasValidNumber;

  function handleSubmit() {
    if (!canSubmit) return;
    onSubmit(unknown ? { known: false } : { known: true, value: parsed });
  }

  return (
    <div className={STYLES.container}>
      <p className={STYLES.label}>{question.label}</p>
      <p className={STYLES.helper}>{question.helper}</p>
      <div className={STYLES.inputRow}>
        <input
          className={STYLES.input}
          type="number"
          inputMode="numeric"
          placeholder={question.placeholder}
          value={text}
          disabled={unknown}
          onChange={(e) => setText(e.target.value)}
          aria-label={question.label}
        />
        <span className={STYLES.unit}>{question.unit}</span>
      </div>
      <div className={STYLES.checkboxRow}>
        <label className={STYLES.checkboxLabel}>
          <input type="checkbox" checked={unknown} onChange={(e) => setUnknown(e.target.checked)} />
          모름
        </label>
      </div>
      <button type="button" className={STYLES.nextButton} disabled={!canSubmit} onClick={handleSubmit}>
        다음
      </button>
    </div>
  );
}

function BoolQuestionBody({
  question,
  defaultAnswer,
  onSubmit,
}: {
  question: BoolQuestion;
  defaultAnswer?: AnswerBool;
  onSubmit: (answer: AnswerBool) => void;
}) {
  type Selection = "true" | "false" | "unknown" | null;
  const initialSelection: Selection =
    defaultAnswer === undefined ? null : defaultAnswer.known === false ? "unknown" : defaultAnswer.value ? "true" : "false";
  const [selection, setSelection] = useState<Selection>(initialSelection);

  const canSubmit = selection !== null;

  function handleSubmit() {
    if (selection === null) return;
    if (selection === "unknown") {
      onSubmit({ known: false });
      return;
    }
    onSubmit({ known: true, value: selection === "true" });
  }

  function optionClassName(active: boolean) {
    return `${STYLES.optionButtonBase} ${active ? STYLES.optionButtonSelected : STYLES.optionButtonUnselected}`;
  }

  return (
    <div className={STYLES.container}>
      <p className={STYLES.label}>{question.label}</p>
      <p className={STYLES.helper}>{question.helper}</p>
      <div className={STYLES.optionRow}>
        <button type="button" className={optionClassName(selection === "true")} onClick={() => setSelection("true")}>
          {question.trueLabel}
        </button>
        <button type="button" className={optionClassName(selection === "false")} onClick={() => setSelection("false")}>
          {question.falseLabel}
        </button>
        <button type="button" className={optionClassName(selection === "unknown")} onClick={() => setSelection("unknown")}>
          모름
        </button>
      </div>
      <button type="button" className={STYLES.nextButton} disabled={!canSubmit} onClick={handleSubmit}>
        다음
      </button>
    </div>
  );
}

function ApproxIntQuestionBody({
  question,
  defaultAnswer,
  onSubmit,
}: {
  question: ApproxIntQuestion;
  defaultAnswer?: AnswerApproxInt;
  onSubmit: (answer: AnswerApproxInt) => void;
}) {
  const [text, setText] = useState(defaultAnswer?.known && defaultAnswer.value !== undefined ? String(defaultAnswer.value) : "");
  const [unknown, setUnknown] = useState(defaultAnswer?.known === false);
  const [approx, setApprox] = useState(defaultAnswer?.approx ?? false);

  const parsed = Number(text);
  const hasValidNumber = text.trim() !== "" && Number.isFinite(parsed);
  const canSubmit = unknown || hasValidNumber;

  function handleSubmit() {
    if (!canSubmit) return;
    onSubmit(unknown ? { known: false } : { known: true, approx, value: parsed });
  }

  return (
    <div className={STYLES.container}>
      <p className={STYLES.label}>{question.label}</p>
      <p className={STYLES.helper}>{question.helper}</p>
      <div className={STYLES.inputRow}>
        <input
          className={STYLES.input}
          type="number"
          inputMode="numeric"
          placeholder={question.placeholder}
          value={text}
          disabled={unknown}
          onChange={(e) => setText(e.target.value)}
          aria-label={question.label}
        />
        <span className={STYLES.unit}>{question.unit}</span>
      </div>
      <div className={STYLES.checkboxRow}>
        <label className={STYLES.checkboxLabel}>
          <input
            type="checkbox"
            checked={approx}
            disabled={unknown}
            onChange={(e) => setApprox(e.target.checked)}
          />
          대략 알아요 (정확한 값은 몰라요)
        </label>
        <label className={STYLES.checkboxLabel}>
          <input type="checkbox" checked={unknown} onChange={(e) => setUnknown(e.target.checked)} />
          모름
        </label>
      </div>
      <button type="button" className={STYLES.nextButton} disabled={!canSubmit} onClick={handleSubmit}>
        다음
      </button>
    </div>
  );
}
