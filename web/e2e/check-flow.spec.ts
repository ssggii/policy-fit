import { test, expect } from "@playwright/test";

/**
 * 체크플로우 E2E — MVP 3정책(주택드림·버팀목·청년월세) 각각의 질문→판정 왕복.
 * 실제 백엔드(POST /verdicts, http://localhost:8080)가 떠 있어야 통과한다 (mock 없음).
 *   cd backend && ./gradlew bootRun &
 *   cd web && pnpm dev &   (또는 playwright webServer가 자동 기동)
 *   cd web && pnpm e2e
 *
 * 소득·자산 값은 항상 임계값에서 충분히 떨어진 값을 쓴다 — 버팀목 asset_self.max_krw(3.45억/3.37억
 * 출처 불일치, 이슈 #33)·청년월세 income_self.max_krw(중위소득 미확정, 이슈 #15)가 아직 잠정값이라
 * 경계값 근처를 찌르면 그 값이 확정될 때 이 e2e가 깨진다(#43 스코프 조정 코멘트 참조).
 */

const DISCLAIMER_TEXT = "이 결과는 입력하신 내용을 바탕으로 한 추정 판정입니다";

async function goToCheck(page: import("@playwright/test").Page, policyName: string | RegExp) {
  await page.goto("/check");
  // #50(정책 선택)부터 /check는 정책 선택 화면을 먼저 보여준다.
  await page.getByRole("button", { name: policyName }).click();
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
  await expect(page.getByText("지금 본인 이름으로 된 집이 없으신가요?")).toBeVisible();
  await page.getByRole("button", { name: "네, 없어요" }).click();
  await page.getByRole("button", { name: "다음" }).click();
}

async function answerLeaseType(page: import("@playwright/test").Page, label: "전세" | "월세") {
  await expect(page.getByText("지금 사는 집은 전세인가요, 월세인가요?")).toBeVisible();
  await page.getByRole("button", { name: label, exact: true }).click();
  await page.getByRole("button", { name: "다음" }).click();
}

async function answerIncome(page: import("@playwright/test").Page, monthlyKrw: string) {
  await expect(page.getByText("본인의 월 소득은 얼마인가요?")).toBeVisible();
  await page.getByRole("spinbutton", { name: "본인의 월 소득은 얼마인가요?" }).fill(monthlyKrw);
  await page.getByRole("button", { name: "다음" }).click();
}

async function answerAsset(page: import("@playwright/test").Page, krw: string) {
  await expect(page.getByText("본인 재산은 얼마나 되나요?")).toBeVisible();
  await page.getByRole("spinbutton", { name: "본인 재산은 얼마나 되나요?" }).fill(krw);
  await page.getByRole("button", { name: "다음" }).click();
}

async function answerMarried(page: import("@playwright/test").Page, married: boolean) {
  await expect(page.getByText("혼인 중이신가요?")).toBeVisible();
  await page.getByRole("button", { name: married ? "네, 혼인 중이에요" : "아니요, 미혼이에요" }).click();
  await page.getByRole("button", { name: "다음" }).click();
}

test.describe("청년 주택드림 청약통장", () => {
  const POLICY_NAME = /청년 주택드림 청약통장/;

  test("age=28 / 무주택 / 월소득 250만 → 가능 + 신청 링크 노출 + 추정 고지", async ({ page }) => {
    await goToCheck(page, POLICY_NAME);
    await answerAge(page, "28");
    await answerHousingNone(page);
    await answerIncome(page, "2500000");

    await expect(page.getByText("가능", { exact: true })).toBeVisible();
    await expect(page.getByRole("link", { name: "공식 신청 페이지로 이동" })).toBeVisible();
    await expect(page.getByText(DISCLAIMER_TEXT)).toBeVisible();
  });

  test("age=28 / 무주택 / 월소득 500만(연 6천만, 5천만 초과) → 부적합 + 신청 링크 없음", async ({ page }) => {
    await goToCheck(page, POLICY_NAME);
    await answerAge(page, "28");
    await answerHousingNone(page);
    await answerIncome(page, "5000000");

    await expect(page.getByText("부적합", { exact: true })).toBeVisible();
    await expect(page.getByRole("link", { name: "공식 신청 페이지로 이동" })).toHaveCount(0);
    await expect(page.getByText(DISCLAIMER_TEXT)).toBeVisible();
  });

  test("나이 '모름' 선택 → 추가 확인 필요 + 추정 고지", async ({ page }) => {
    await goToCheck(page, POLICY_NAME);
    await answerAgeUnknown(page);
    await answerHousingNone(page);
    await answerIncome(page, "2500000");

    await expect(page.getByText("추가 확인 필요", { exact: true })).toBeVisible();
    await expect(page.getByText(DISCLAIMER_TEXT)).toBeVisible();
  });
});

test.describe("청년전용 버팀목 전세자금대출", () => {
  const POLICY_NAME = /버팀목 전세자금대출/;

  test("age=28 / 무주택 / 전세 / 월소득 300만 / 재산 3천만 / 미혼 → 가능", async ({ page }) => {
    await goToCheck(page, POLICY_NAME);
    await answerAge(page, "28");
    await answerHousingNone(page);
    await answerLeaseType(page, "전세");
    await answerIncome(page, "3000000");
    await answerAsset(page, "30000000");
    await answerMarried(page, false);

    await expect(page.getByText("가능", { exact: true })).toBeVisible();
    await expect(page.getByRole("link", { name: "공식 신청 페이지로 이동" })).toBeVisible();
    await expect(page.getByText(DISCLAIMER_TEXT)).toBeVisible();
  });

  test("월세 임차(전세 아님) → 부적합 + 신청 링크 없음", async ({ page }) => {
    await goToCheck(page, POLICY_NAME);
    await answerAge(page, "28");
    await answerHousingNone(page);
    await answerLeaseType(page, "월세");
    await answerIncome(page, "3000000");
    await answerAsset(page, "30000000");
    await answerMarried(page, false);

    await expect(page.getByText("부적합", { exact: true })).toBeVisible();
    await expect(page.getByRole("link", { name: "공식 신청 페이지로 이동" })).toHaveCount(0);
    await expect(page.getByText(DISCLAIMER_TEXT)).toBeVisible();
  });

  // 이슈 #11, ADR-0005 D5 재사용(ADR-0007) — 기혼은 부부합산 소득 평가가 필요해 자가판정 불가.
  test("age=28 / 무주택 / 전세 / 월소득 300만 / 재산 3천만 / 기혼(부부합산 필요) → 자가판정 불가", async ({ page }) => {
    await goToCheck(page, POLICY_NAME);
    await answerAge(page, "28");
    await answerHousingNone(page);
    await answerLeaseType(page, "전세");
    await answerIncome(page, "3000000");
    await answerAsset(page, "30000000");
    await answerMarried(page, true);

    await expect(page.getByText("자가판정 불가", { exact: true })).toBeVisible();
    await expect(page.getByText("부적합이 아니라 자가판정 불가예요")).toBeVisible();
    await expect(page.getByText("판정 근거")).toHaveCount(0);
    await expect(page.getByRole("link", { name: "공식 신청 페이지로 이동" })).toHaveCount(0);
    await expect(page.getByText(DISCLAIMER_TEXT)).toBeVisible();
  });
});

test.describe("청년월세 특별지원", () => {
  const POLICY_NAME = /청년월세 특별지원/;

  // DOMAIN §5.3·ADR-0005 D5 — out_of_scope_gate(age>=30 or married)는 "원가구 소득 심사 면제"
  // 경로다. 게이트 TRUE(혼인)면 면제돼 자가판정(본인 소득만) 가능 → 가능. 게이트 FALSE(30세
  // 미만·미혼, 아래 두 번째 테스트)면 원가구 소득 확인이 필요해 자가판정 불가 → out_of_scope.
  test("age=25 / 무주택 / 월세 / 월소득 150만 / 혼인 중(원가구 소득 면제) → 가능", async ({ page }) => {
    await goToCheck(page, POLICY_NAME);
    await answerAge(page, "25");
    await answerHousingNone(page);
    await answerLeaseType(page, "월세");
    await answerIncome(page, "1500000");
    await answerMarried(page, true);

    await expect(page.getByText("가능", { exact: true })).toBeVisible();
    await expect(page.getByRole("link", { name: "공식 신청 페이지로 이동" })).toBeVisible();
    await expect(page.getByText(DISCLAIMER_TEXT)).toBeVisible();
  });

  test("age=25 / 무주택 / 월세 / 월소득 150만 / 미혼(원가구 소득 면제 불성립) → 자가판정 불가 + 부적합과 구분되는 안내 + 판정 근거 섹션 없음", async ({
    page,
  }) => {
    await goToCheck(page, POLICY_NAME);
    await answerAge(page, "25");
    await answerHousingNone(page);
    await answerLeaseType(page, "월세");
    await answerIncome(page, "1500000");
    await answerMarried(page, false);

    await expect(page.getByText("자가판정 불가", { exact: true })).toBeVisible();
    await expect(page.getByText("부적합이 아니라 자가판정 불가예요")).toBeVisible();
    await expect(page.getByText("판정 근거")).toHaveCount(0);
    await expect(page.getByRole("link", { name: "공식 신청 페이지로 이동" })).toHaveCount(0);
    await expect(page.getByText(DISCLAIMER_TEXT)).toBeVisible();
  });
});
