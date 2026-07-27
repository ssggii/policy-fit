import { test, expect } from "@playwright/test";

/**
 * 청년 주택드림 청약통장 체크플로우 E2E.
 * 실제 백엔드(POST /verdicts, http://localhost:8080)가 떠 있어야 통과한다 (mock 없음).
 *   cd backend && ./gradlew bootRun &
 *   cd web && pnpm dev &   (또는 playwright webServer가 자동 기동)
 *   cd web && pnpm e2e
 */

const DISCLAIMER_TEXT = "이 결과는 입력하신 내용을 바탕으로 한 추정 판정입니다";

async function goToCheck(page: import("@playwright/test").Page) {
  await page.goto("/check");
  await expect(page.getByText("나이가 어떻게 되세요?")).toBeVisible();
}

async function answerAge(page: import("@playwright/test").Page, value: string) {
  await page.getByRole("spinbutton", { name: "나이가 어떻게 되세요?" }).fill(value);
  await page.getByRole("button", { name: "다음" }).click();
}

async function answerAgeUnknown(page: import("@playwright/test").Page) {
  await page.getByRole("checkbox", { name: "모름" }).check();
  await page.getByRole("button", { name: "다음" }).click();
}

async function answerHousingNone(page: import("@playwright/test").Page) {
  await expect(page.getByText("지금 본인 이름으로 된 집을 갖고 있나요?")).toBeVisible();
  await page.getByRole("button", { name: "아니요, 없어요" }).click();
  await page.getByRole("button", { name: "다음" }).click();
}

async function answerIncome(page: import("@playwright/test").Page, monthlyKrw: string) {
  await expect(page.getByText("본인의 월 소득은 얼마인가요?")).toBeVisible();
  await page.getByRole("spinbutton", { name: "본인의 월 소득은 얼마인가요?" }).fill(monthlyKrw);
  await page.getByRole("button", { name: "다음" }).click();
}

test("age=28 / 무주택 / 월소득 250만 → 가능 + 신청 링크 노출 + 추정 고지", async ({ page }) => {
  await goToCheck(page);
  await answerAge(page, "28");
  await answerHousingNone(page);
  await answerIncome(page, "2500000");

  await expect(page.getByText("가능", { exact: true })).toBeVisible();
  await expect(page.getByRole("link", { name: "공식 신청 페이지로 이동" })).toBeVisible();
  await expect(page.getByText(DISCLAIMER_TEXT)).toBeVisible();
});

test("age=28 / 무주택 / 월소득 500만(연 6천만, 5천만 초과) → 부적합 + 신청 링크 없음", async ({ page }) => {
  await goToCheck(page);
  await answerAge(page, "28");
  await answerHousingNone(page);
  await answerIncome(page, "5000000");

  await expect(page.getByText("부적합", { exact: true })).toBeVisible();
  await expect(page.getByRole("link", { name: "공식 신청 페이지로 이동" })).toHaveCount(0);
  await expect(page.getByText(DISCLAIMER_TEXT)).toBeVisible();
});

test("나이 '모름' 선택 → 추가 확인 필요 + 추정 고지", async ({ page }) => {
  await goToCheck(page);
  await answerAgeUnknown(page);
  await answerHousingNone(page);
  await answerIncome(page, "2500000");

  await expect(page.getByText("추가 확인 필요", { exact: true })).toBeVisible();
  await expect(page.getByText(DISCLAIMER_TEXT)).toBeVisible();
});
