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

/**
 * 질문 화면은 중앙 520 컬럼(docs/design/polfit-design.html 반응형 규칙). PolicySelect·ResultView는
 * 각자 자기 폭을 갖고 있어(880/560) 이 컴포넌트가 감싸지 않는다.
 */
const STYLES = {
  flowColumn: "mx-auto flex w-full max-w-[520px] flex-col gap-4",
  topRow: "flex items-center justify-between gap-2",
  policyName: "text-sm font-semibold text-muted",
  stepLabel: "font-mono text-xs font-semibold tabular-nums text-faint",
  progressTrack: "h-1.5 w-full overflow-hidden rounded-full bg-line",
  progressFill: "transition-app h-full rounded-full bg-blue",
  backButton: "self-start text-[13px] font-semibold text-faint transition-app hover:text-muted",
  loading: "mx-auto w-full max-w-[520px] text-sm text-muted",
  errorBox: "mx-auto flex w-full max-w-[520px] flex-col gap-3",
  errorText: "text-sm font-medium text-ineligible",
  retryButton:
    "self-start rounded-input border-[1.5px] border-line px-4 py-2.5 text-sm font-bold text-ink transition-app hover:border-blue",
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

  const progressPercent = ((stepIndex + 1) / stepOrder.length) * 100;

  return (
    <div className={STYLES.flowColumn}>
      <div className={STYLES.topRow}>
        <p className={STYLES.policyName}>{policy.name}</p>
        <span className={STYLES.stepLabel}>
          {stepIndex + 1} / {stepOrder.length}
        </span>
      </div>
      <div className={STYLES.progressTrack}>
        <div className={STYLES.progressFill} style={{ width: `${progressPercent}%` }} />
      </div>
      <button type="button" className={STYLES.backButton} onClick={handleBack}>
        {stepIndex === 0 ? (
          "정책 다시 고르기"
        ) : (
          <>
            <span aria-hidden>{"← "}</span>이전 질문으로
          </>
        )}
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
