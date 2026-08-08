"use client";

import { useState } from "react";
import { postVerdict, VerdictApiError } from "@/lib/api/verdicts";
import type { PolicyId, VerdictResult } from "@/lib/types/verdict";
import QuestionStep from "./QuestionStep";
import ResultView from "./ResultView";
import PolicySelect from "./PolicySelect";
import { QUESTIONS } from "./questions";
import { POLICIES } from "./policies";
import { toVerdictRequestAnswers, type AnyAnswer, type CollectedAnswers } from "./answers";

type Status = "selecting" | "answering" | "loading" | "error" | "done";

const STYLES = {
  page: "flex flex-col gap-6",
  policyName: "text-sm font-medium text-zinc-500",
  progress: "text-sm text-zinc-400",
  backButton: "self-start text-sm text-zinc-500 underline",
  loading: "text-sm text-zinc-500",
  errorBox: "flex flex-col gap-3",
  errorText: "text-sm text-red-700",
  retryButton: "self-start rounded border border-zinc-300 px-4 py-2 text-sm",
};

/**
 * 체크플로우 상태머신. 정책 선택 → 그 정책의 questionOrder(policies.ts)대로 질문 → 마지막 답변
 * 즉시 POST /verdicts(선택한 policy_id)를 호출한다. 판정은 여기서 계산하지 않고 백엔드 응답을
 * 그대로 ResultView에 넘긴다.
 */
export default function CheckFlow() {
  const [policyId, setPolicyId] = useState<PolicyId | null>(null);
  const [stepIndex, setStepIndex] = useState(0);
  const [answers, setAnswers] = useState<CollectedAnswers>({});
  const [status, setStatus] = useState<Status>("selecting");
  const [result, setResult] = useState<VerdictResult | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const policy = policyId ? POLICIES.find((p) => p.id === policyId) : undefined;
  const stepOrder = policy?.questionOrder ?? [];
  const currentKey = stepOrder[stepIndex];
  const currentQuestion = currentKey ? QUESTIONS.find((q) => q.key === currentKey) : undefined;

  function handleSelectPolicy(id: PolicyId) {
    setPolicyId(id);
    setStepIndex(0);
    setAnswers({});
    setResult(null);
    setErrorMessage(null);
    setStatus("answering");
  }

  function handleSubmit(answer: AnyAnswer) {
    if (!currentKey) return;
    // currentKey에 해당하는 질문 타입이 렌더한 답만 들어온다(QuestionStep 디스패치) → 키-값 일치.
    const nextAnswers: CollectedAnswers = { ...answers, [currentKey]: answer };
    setAnswers(nextAnswers);

    if (stepIndex < stepOrder.length - 1) {
      setStepIndex(stepIndex + 1);
      return;
    }
    void submit(nextAnswers);
  }

  async function submit(finalAnswers: CollectedAnswers) {
    if (!policyId) return;
    setStatus("loading");
    setErrorMessage(null);
    try {
      const response = await postVerdict({
        policy_id: policyId,
        answers: toVerdictRequestAnswers(finalAnswers),
      });
      setResult(response);
      setStatus("done");
    } catch (error) {
      setErrorMessage(error instanceof VerdictApiError ? error.message : "판정을 불러오지 못했습니다. 다시 시도해주세요.");
      setStatus("error");
    }
  }

  function handleBack() {
    if (stepIndex === 0) {
      setPolicyId(null);
      setStatus("selecting");
      return;
    }
    setStepIndex(stepIndex - 1);
  }

  if (status === "selecting" || !policy || !currentQuestion) {
    return <PolicySelect onSelect={handleSelectPolicy} />;
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
      <p className={STYLES.policyName}>{policy.name}</p>
      <p className={STYLES.progress}>
        {stepIndex + 1} / {stepOrder.length}
      </p>
      <button type="button" className={STYLES.backButton} onClick={handleBack}>
        {stepIndex === 0 ? "정책 다시 고르기" : "이전 질문으로"}
      </button>
      <QuestionStep
        key={currentQuestion.key}
        question={currentQuestion}
        defaultAnswer={answers[currentKey]}
        onSubmit={handleSubmit}
      />
    </div>
  );
}
