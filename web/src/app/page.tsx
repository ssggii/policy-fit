import Link from "next/link";

/**
 * 랜딩 페이지. 스타일은 최소한만 — 와이어프레임 확정 전 기능 스켈레톤 단계(디자인은 나중에 통째로 교체).
 */
export default function Home() {
  return (
    <main className="mx-auto flex w-full max-w-xl flex-1 flex-col items-start justify-center gap-6 px-6 py-12">
      <h1 className="text-2xl font-semibold">폴핏 (policy-fit)</h1>
      <p className="text-zinc-600">
        지금 신청할 수 있는 청년 정책인지, 몇 가지 질문에 답하면 바로 확인할 수 있어요.
      </p>
      <Link href="/check" className="rounded bg-zinc-900 px-5 py-2 text-white">
        체크 시작하기
      </Link>
    </main>
  );
}
