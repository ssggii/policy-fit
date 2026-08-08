"use client";

import { useState } from "react";
import type { AnswerApproxInt, AnswerBool, AnswerInt, AnswerString } from "@/lib/types/verdict";
import type { ApproxIntQuestion, BoolQuestion, IntQuestion, Question, SelectQuestion } from "./questions";
import type { AnyAnswer } from "./answers";

/**
 * 스타일은 여기 한 곳에 상수로 몰아둔다 — 디자인이 바뀌면 이 맵만 교체하면 되게.
 * 로직(상태·이벤트 핸들러·role·접근성 이름)은 그대로 두고 className만 디자인 토큰(globals.css)으로 교체했다.
 * 참고: docs/design/polfit-design.html의 q-title/q-help/field/choice/seg 컴포넌트.
 */
const STYLES = {
  container: "flex flex-col gap-4",
  label: "mt-0.5 text-[22px] font-extrabold leading-[1.25] tracking-[-0.03em] text-ink",
  helper: "text-[13px] leading-relaxed text-muted",
  // .field — 입력 한 줄. focus-within으로 blue 링을 준다(별도 focus state 불필요).
  inputRow:
    "flex items-center gap-2 rounded-input border-[1.5px] border-line bg-surface px-[15px] py-[14px] transition-app focus-within:border-blue focus-within:shadow-[0_0_0_4px_var(--blue-wash)]",
  input:
    "w-full min-w-0 bg-transparent text-[17px] font-semibold text-ink outline-none placeholder:font-medium placeholder:text-faint disabled:text-faint",
  unit: "ml-auto flex-none text-sm font-semibold text-faint",
  checkboxRow: "flex flex-wrap items-center gap-2",
  // 네이티브 체크박스는 눈에 보이지 않게(opacity-0) 라벨 전체를 덮게 겹쳐, 라벨을 seg 필(pill)처럼 그린다.
  // sr-only(0px 크기)로 숨기면 클릭 좌표가 라벨과 겹쳐 실제 브라우저 클릭(E2E)이 라벨에 가로막힌다 —
  // absolute+inset-0으로 히트 영역을 라벨 전체로 넓혀서 피한다. role="checkbox"·접근성 이름은 그대로 유지된다.
  chipLabelBase:
    "relative inline-flex cursor-pointer select-none items-center gap-1.5 rounded-full border-[1.5px] px-3 py-2 text-[12.5px] font-bold transition-app has-[:disabled]:cursor-not-allowed has-[:disabled]:opacity-50",
  chipLabelOn: "border-ink bg-ink text-bg",
  chipLabelOff: "border-line text-muted",
  chipInput: "absolute inset-0 h-full w-full cursor-pointer opacity-0",
  // .choices/.choice — 세로로 쌓인 선택 행. 선택되면 파란 테두리+wash, 원형 라디오 표시로 시각 강조.
  optionRow: "flex flex-col gap-2.5",
  optionButtonBase:
    "flex w-full items-center gap-3 rounded-input border-[1.5px] px-4 py-[15px] text-left text-base font-semibold transition-app",
  optionButtonSelected: "border-blue bg-blue-wash text-ink",
  optionButtonUnselected: "border-line bg-surface text-ink hover:border-blue/50",
  radioBase: "grid h-5 w-5 flex-none place-items-center rounded-full border-2",
  radioSelected: "border-blue",
  radioUnselected: "border-line",
  radioDot: "h-2.5 w-2.5 rounded-full bg-blue",
  nextButton:
    "mt-2 flex w-full items-center justify-center gap-2 rounded-input bg-blue px-[15px] py-[15px] text-base font-bold text-on-blue transition-app hover:bg-blue-press disabled:cursor-not-allowed disabled:bg-line disabled:text-faint",
};

export interface QuestionStepProps {
  question: Question;
  /** 뒤로가기 등으로 이 질문에 이미 답한 적이 있으면 초기값으로 채운다. */
  defaultAnswer?: AnyAnswer;
  onSubmit: (answer: AnyAnswer) => void;
}

/**
 * 질문 1개를 보여준다. question.type에 따라 int(나이) / bool(무주택·혼인) / approx_int(소득·재산)
 * / select(임차 형태) 중 하나의 입력 UI를 렌더링한다. 모든 값은 '모름'으로 응답 가능(F-001 수용 기준).
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
  if (question.type === "select") {
    return (
      <SelectQuestionBody
        question={question}
        defaultAnswer={defaultAnswer as AnswerString | undefined}
        onSubmit={onSubmit}
      />
    );
  }
  return <IntQuestionBody question={question} defaultAnswer={defaultAnswer as AnswerInt | undefined} onSubmit={onSubmit} />;
}

/** '모름' 체크박스를 seg 필 모양으로 그리는 공용 조각. role="checkbox"·name은 그대로 유지된다. */
function UnknownChip({ checked, onChange }: { checked: boolean; onChange: (checked: boolean) => void }) {
  return (
    <label className={`${STYLES.chipLabelBase} ${checked ? STYLES.chipLabelOn : STYLES.chipLabelOff}`}>
      <input
        type="checkbox"
        className={STYLES.chipInput}
        checked={checked}
        onChange={(e) => onChange(e.target.checked)}
      />
      모름
    </label>
  );
}

/** 선택 행(choice) 앞의 원형 라디오 표시 — 순수 표시용, 렌더 중 재생성되지 않도록 모듈 스코프에 둔다. */
function Radio({ active }: { active: boolean }) {
  return (
    <span className={`${STYLES.radioBase} ${active ? STYLES.radioSelected : STYLES.radioUnselected}`}>
      {active && <span className={STYLES.radioDot} />}
    </span>
  );
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
      <h1 className={STYLES.label}>{question.label}</h1>
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
        <UnknownChip checked={unknown} onChange={setUnknown} />
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
      <h1 className={STYLES.label}>{question.label}</h1>
      <p className={STYLES.helper}>{question.helper}</p>
      <div className={STYLES.optionRow}>
        <button type="button" className={optionClassName(selection === "true")} onClick={() => setSelection("true")}>
          <Radio active={selection === "true"} />
          {question.trueLabel}
        </button>
        <button type="button" className={optionClassName(selection === "false")} onClick={() => setSelection("false")}>
          <Radio active={selection === "false"} />
          {question.falseLabel}
        </button>
        <button type="button" className={optionClassName(selection === "unknown")} onClick={() => setSelection("unknown")}>
          <Radio active={selection === "unknown"} />
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
      <h1 className={STYLES.label}>{question.label}</h1>
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
        <label className={`${STYLES.chipLabelBase} ${approx && !unknown ? STYLES.chipLabelOn : STYLES.chipLabelOff}`}>
          <input
            type="checkbox"
            className={STYLES.chipInput}
            checked={approx}
            disabled={unknown}
            onChange={(e) => setApprox(e.target.checked)}
          />
          대략 알아요 (정확한 값은 몰라요)
        </label>
        <UnknownChip checked={unknown} onChange={setUnknown} />
      </div>
      <button type="button" className={STYLES.nextButton} disabled={!canSubmit} onClick={handleSubmit}>
        다음
      </button>
    </div>
  );
}

function SelectQuestionBody({
  question,
  defaultAnswer,
  onSubmit,
}: {
  question: SelectQuestion;
  defaultAnswer?: AnswerString;
  onSubmit: (answer: AnswerString) => void;
}) {
  type Selection = { kind: "value"; value: string } | { kind: "unknown" } | null;
  const initialSelection: Selection =
    defaultAnswer === undefined
      ? null
      : defaultAnswer.known === false
        ? { kind: "unknown" }
        : defaultAnswer.value !== undefined
          ? { kind: "value", value: defaultAnswer.value }
          : null;
  const [selection, setSelection] = useState<Selection>(initialSelection);

  const canSubmit = selection !== null;
  const selectedValue = selection?.kind === "value" ? selection.value : null;

  function handleSubmit() {
    if (selection === null) return;
    if (selection.kind === "unknown") {
      onSubmit({ known: false });
      return;
    }
    onSubmit({ known: true, value: selection.value });
  }

  function optionClassName(active: boolean) {
    return `${STYLES.optionButtonBase} ${active ? STYLES.optionButtonSelected : STYLES.optionButtonUnselected}`;
  }

  return (
    <div className={STYLES.container}>
      <h1 className={STYLES.label}>{question.label}</h1>
      <p className={STYLES.helper}>{question.helper}</p>
      <div className={STYLES.optionRow}>
        {question.options.map((opt) => (
          <button
            key={opt.value}
            type="button"
            className={optionClassName(selectedValue === opt.value)}
            onClick={() => setSelection({ kind: "value", value: opt.value })}
          >
            <Radio active={selectedValue === opt.value} />
            {opt.label}
          </button>
        ))}
        <button
          type="button"
          className={optionClassName(selection?.kind === "unknown")}
          onClick={() => setSelection({ kind: "unknown" })}
        >
          <Radio active={selection?.kind === "unknown"} />
          모름
        </button>
      </div>
      <button type="button" className={STYLES.nextButton} disabled={!canSubmit} onClick={handleSubmit}>
        다음
      </button>
    </div>
  );
}
