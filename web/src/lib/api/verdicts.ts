import type { ErrorResponse, VerdictRequest, VerdictResult } from "@/lib/types/verdict";

/**
 * 백엔드 base URL. 배포 오리진은 미정(ADR-0004 D6)이라 환경변수로 주입하고,
 * 로컬 개발 기본값은 백엔드 기본 포트(8080)로 둔다.
 */
const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL ?? "http://localhost:8080";

export class VerdictApiError extends Error {}

/**
 * POST /verdicts 단일 호출 지점.
 * 판정 로직은 여기 없다 — 응답을 그대로 반환만 한다 (ARCHITECTURE: 판정은 백엔드 호출로 수행).
 */
export async function postVerdict(request: VerdictRequest): Promise<VerdictResult> {
  const response = await fetch(`${BACKEND_URL}/verdicts`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new VerdictApiError(await extractErrorMessage(response));
  }

  return (await response.json()) as VerdictResult;
}

async function extractErrorMessage(response: Response): Promise<string> {
  try {
    const body = (await response.json()) as ErrorResponse;
    if (body?.message) return body.message;
  } catch {
    // 본문이 JSON이 아니면 상태 코드로 대체 메시지를 만든다.
  }
  return `요청이 실패했습니다 (${response.status})`;
}
