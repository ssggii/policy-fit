import type { Metadata } from "next";
import "./globals.css";

/**
 * 폰트는 next/font(Google) 대신 한글 시스템 스택(globals.css --font-sans)을 그대로 쓴다 —
 * 외부 CDN 의존·CSP 이슈 없이, OS가 이미 갖고 있는 서체(Apple SD Gothic Neo 등)로 충분하다.
 */
export const metadata: Metadata = {
  title: "폴핏 — 지금 신청 가능한 청년 정책, 빠짐없이",
  description:
    "복잡한 자격 요건을 대신 따져 지금 신청할 수 있는 청년 정책인지 알려드려요. 인증 없이, 저장 없이, 약 1분.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" className="h-full antialiased">
      <body className="flex min-h-full flex-col bg-bg text-ink">{children}</body>
    </html>
  );
}
