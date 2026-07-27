"use client";

import { useState } from "react";
import { postVerdict, VerdictApiError } from "@/lib/api/verdicts";
import type { AnswerApproxInt, AnswerBool, AnswerInt, VerdictResult } from "@/lib/types/verdict";
import QuestionStep from "./QuestionStep";
import ResultView from "./ResultView";
import { QUESTIONS, type QuestionKey } from "./questions";

const STEP_ORDER: QuestionKey[] = ["age", "housing_none", "income_self"];

interface Answers {
  age?: AnswerInt;
  housing_none?: AnswerBool;
  income_self?: AnswerApproxInt;
}

type Status = "answering" | "loading" | "error" | "done";

const STYLES = {
  page: "flex flex-col gap-6",
  progress: "text-sm text-zinc-400",
  backButton: "self-start text-sm text-zinc-500 underline",
  loading: "text-sm text-zinc-500",
  errorBox: "flex flex-col gap-3",
  errorText: "text-sm text-red-700",
  retryButton: "self-start rounded border border-zinc-300 px-4 py-2 text-sm",
};

/**
 * 청년 주택드림 청약통장 체크플로우 단계 상태머신.
 * age → housing_none → income_self 순서로 질문하고, 마지막 질문 답변 즉시 POST /verdicts를 호출한다.
 * 판정 자체는 여기서 계산하지 않는다 — 백엔드 응답(verdict.state)을 그대로 ResultView에 넘긴다.
 */
export default function CheckFlow() {
  const [stepIndex, setStepIndex] = useState(0);
  const [answers, setAnswers] = useState<Answers>({});
  const [status, setStatus] = useState<Status>("answering");
  const [result, setResult] = useState<VerdictResult | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const currentKey = STEP_ORDER[stepIndex];
  const currentQuestion = QUESTIONS.find((q) => q.key === currentKey)!;

  function handleSubmit(answer: AnswerInt | AnswerBool | AnswerApproxInt) {
    const nextAnswers: Answers = { ...answers, [currentKey]: answer };
    setAnswers(nextAnswers);

    if (stepIndex < STEP_ORDER.length - 1) {
      setStepIndex(stepIndex + 1);
      return;
    }
    void submit(nextAnswers);
  }

  async function submit(finalAnswers: Answers) {
    setStatus("loading");
    setErrorMessage(null);
    try {
      const response = await postVerdict({
        policy_id: "jutaek-dream",
        answers: {
          age: finalAnswers.age,
          housing_none: finalAnswers.housing_none,
          income_self_monthly_krw: finalAnswers.income_self,
        },
      });
      setResult(response);
      setStatus("done");
    } catch (error) {
      setErrorMessage(error instanceof VerdictApiError ? error.message : "판정을 불러오지 못했습니다. 다시 시도해주세요.");
      setStatus("error");
    }
  }

  function handleBack() {
    if (stepIndex === 0) return;
    setStepIndex(stepIndex - 1);
  }

  if (status === "done" && result) {
    return <ResultView result={result} />;
  }

  if (status === "loading") {
    return <p className={STYLES.loading}>판정 결과를 확인하는 중이에요...</p>;
  }

  if (status === "error") {
    return (
      <div className={STYLES.errorBox}>
        <p className={STYLES.errorText}>{errorMessage}</p>
        <button type="button" className={STYLES.retryButton} onClick={() => void submit(answers)}>
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className={STYLES.page}>
      <p className={STYLES.progress}>
        {stepIndex + 1} / {STEP_ORDER.length}
      </p>
      {stepIndex > 0 && (
        <button type="button" className={STYLES.backButton} onClick={handleBack}>
          이전 질문으로
        </button>
      )}
      <QuestionStep
        key={currentQuestion.key}
        question={currentQuestion}
        defaultAnswer={answers[currentKey]}
        onSubmit={handleSubmit}
      />
    </div>
  );
}
