import { defineConfig, devices } from "@playwright/test";

/**
 * E2E는 실제 백엔드(POST /verdicts, :8080)가 떠 있어야 통과한다 — mock 없음.
 * 프론트 개발 서버(:3000)는 webServer로 자동 기동한다. 백엔드는 별도로 미리 띄워야 한다.
 */
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  retries: 0,
  reporter: "list",
  use: {
    baseURL: "http://localhost:3000",
    trace: "on-first-retry",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
  webServer: {
    command: "pnpm dev",
    url: "http://localhost:3000",
    reuseExistingServer: true,
    timeout: 60_000,
  },
});
