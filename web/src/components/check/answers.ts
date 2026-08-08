/**
 * 체크플로우가 QuestionKey 기준으로 모은 답변을 `VerdictRequestAnswers`(계약 필드명)로 변환한다.
 * 필드명이 다른 건 income_self→income_self_monthly_krw, asset_self→asset_self_krw 둘뿐이고
 * 나머지(age, housing_none, lease_type, married)는 키가 그대로 같다.
 */

import type { AnswerApproxInt, AnswerBool, AnswerInt, AnswerString, VerdictRequestAnswers } from "@/lib/types/verdict";
import type { QuestionKey } from "./questions";

export type AnyAnswer = AnswerInt | AnswerBool | AnswerApproxInt | AnswerString;

/**
 * QuestionKey별 답 타입을 고정한다. 이 매핑 덕에 아래 toVerdictRequestAnswers의 "읽기"(리터럴 키
 * 접근)는 캐스팅 없이 키-값 타입이 보장된다.
 * 단 CheckFlow의 "쓰기"({ ...prev, [currentKey]: answer })는 currentKey가 QuestionKey 유니온이라
 * TS가 검사를 완화해(계산된 키 + 스프레드의 알려진 한계) 컴파일타임에 잡지 못한다 — 그 경로의
 * 정합은 QuestionStep 디스패치(질문 타입이 렌더한 답만 반환)라는 런타임 불변식에 의존한다.
 * QuestionKey가 늘어나면 여기도 채워야 tsc가 통과한다.
 */
interface AnswerByKey {
  age: AnswerInt;
  housing_none: AnswerBool;
  income_self: AnswerApproxInt;
  lease_type: AnswerString;
  asset_self: AnswerApproxInt;
  married: AnswerBool;
}

/** 체크플로우 진행 중 상태에 두는 답변 맵 — 키별로 정확한 답 타입을 강제한다. */
export type CollectedAnswers = { [K in QuestionKey]?: AnswerByKey[K] };

export function toVerdictRequestAnswers(answers: CollectedAnswers): VerdictRequestAnswers {
  const result: VerdictRequestAnswers = {};
  if (answers.age) result.age = answers.age;
  if (answers.housing_none) result.housing_none = answers.housing_none;
  if (answers.income_self) result.income_self_monthly_krw = answers.income_self;
  if (answers.lease_type) result.lease_type = answers.lease_type;
  if (answers.asset_self) result.asset_self_krw = answers.asset_self;
  if (answers.married) result.married = answers.married;
  return result;
}
